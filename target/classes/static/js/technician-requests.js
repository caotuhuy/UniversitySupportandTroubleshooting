// ============================================================
// EAUT SUPPORT — Technician Dashboard Script
// Đại học Công nghệ Đông Á
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
    let currentUserId = null;
    let allAssignments = [];
    let filteredAssignments = [];
    let currentFilter = 'ALL';
    let currentActionType = null;
    let currentTargetRequestId = null;
    let currentPage = 1;
    const pageSize = 10;

    const techLoading = document.getElementById('techLoading');
    const techEmpty = document.getElementById('techEmpty');
    const techContent = document.getElementById('techContent');
    const taskTableBody = document.getElementById('taskTableBody');
    const techAlert = document.getElementById('techAlert');
    const filterChips = document.querySelectorAll('.filter-chips .filter-chip');
    const searchInput = document.getElementById('techSearchInput');
    const priorityFilter = document.getElementById('techPriorityFilter');
    const btnRefreshTasks = document.getElementById('btnRefreshTasks');

    // KPI elements
    const kpiTotal = document.getElementById('kpiTotal');
    const kpiAssigned = document.getElementById('kpiAssigned');
    const kpiInProgress = document.getElementById('kpiInProgress');
    const kpiCompleted = document.getElementById('kpiCompleted');
    const kpiOverdue = document.getElementById('kpiOverdue');

    // Pagination elements
    const techPagination = document.getElementById('techPagination');
    const techPaginationInfo = document.getElementById('techPaginationInfo');
    const techPaginationControls = document.getElementById('techPaginationControls');

    // Action Modal
    const actionModal = document.getElementById('techActionModal');
    const actionModalTitle = document.getElementById('actionModalTitle');
    const actionModalDesc = document.getElementById('actionModalDesc');
    const actionNoteInput = document.getElementById('actionNoteInput');
    const btnActionModalSubmit = document.getElementById('btnActionModalSubmit');

    const statusLabels = {
        PENDING: 'Chờ tiếp nhận',
        RECEIVED: 'Đã tiếp nhận',
        ASSIGNED: 'Chờ nhận việc',
        IN_PROGRESS: 'Đang xử lý',
        WAITING_INFO: 'Chờ thông tin',
        COMPLETED: 'Đã hoàn thành',
        REJECTED: 'Từ chối',
        CANCELLED: 'Đã hủy'
    };

    const priorityLabels = {
        LOW: 'Thấp',
        MEDIUM: 'Trung bình',
        HIGH: 'Cao',
        URGENT: 'Khẩn cấp'
    };

    const formatDateTime = (val) => {
        if (!val) return '—';
        const d = new Date(val);
        if (Number.isNaN(d.getTime())) return '—';
        const day = String(d.getDate()).padStart(2, '0');
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const year = d.getFullYear();
        const hours = String(d.getHours()).padStart(2, '0');
        const mins = String(d.getMinutes()).padStart(2, '0');
        return `${day}/${month}/${year} ${hours}:${mins}`;
    };

    const updateCounts = () => {
        const counts = {
            ALL: allAssignments.length,
            ASSIGNED: 0,
            IN_PROGRESS: 0,
            WAITING_INFO: 0,
            COMPLETED: 0,
            OVERDUE: 0
        };

        allAssignments.forEach(a => {
            const st = a.requestStatus || a.request?.status;
            if (counts[st] !== undefined) counts[st]++;
            if (st === 'ASSIGNED') counts.ASSIGNED++;
            if (st === 'IN_PROGRESS') counts.IN_PROGRESS++;
            if (st === 'WAITING_INFO') counts.WAITING_INFO++;
            if (st === 'COMPLETED') counts.COMPLETED++;

            const req = a.request || {};
            if (req.isOverdue || req.slaStatus === 'OVERDUE' || (req.resolutionDeadline && new Date(req.resolutionDeadline) < new Date() && st !== 'COMPLETED')) {
                counts.OVERDUE++;
            }
        });

        // Filter chips counts
        document.getElementById('countAll').textContent = counts.ALL;
        document.getElementById('countAssigned').textContent = counts.ASSIGNED;
        document.getElementById('countInProgress').textContent = counts.IN_PROGRESS;
        document.getElementById('countWaiting').textContent = counts.WAITING_INFO;
        document.getElementById('countCompleted').textContent = counts.COMPLETED;

        // KPI metrics
        if (kpiTotal) kpiTotal.textContent = counts.ALL;
        if (kpiAssigned) kpiAssigned.textContent = counts.ASSIGNED;
        if (kpiInProgress) kpiInProgress.textContent = counts.IN_PROGRESS;
        if (kpiCompleted) kpiCompleted.textContent = counts.COMPLETED;
        if (kpiOverdue) kpiOverdue.textContent = counts.OVERDUE;
    };

    const applyFilters = () => {
        const keyword = (searchInput?.value || '').trim().toLowerCase();
        const priorityVal = priorityFilter?.value || '';

        filteredAssignments = allAssignments.filter(item => {
            const req = item.request || {};
            const st = item.requestStatus || req.status || 'ASSIGNED';
            const pr = item.requestPriority || req.priority || 'MEDIUM';

            // Filter chips
            if (currentFilter !== 'ALL' && st !== currentFilter) return false;

            // Priority select
            if (priorityVal && pr !== priorityVal) return false;

            // Keyword
            if (keyword) {
                const code = (item.requestCode || req.requestCode || '').toLowerCase();
                const title = (item.requestTitle || req.title || '').toLowerCase();
                const requester = (item.requesterName || req.requester?.fullName || '').toLowerCase();
                const room = (item.roomName || req.room?.roomName || req.room?.roomCode || '').toLowerCase();
                const matches = code.includes(keyword) || title.includes(keyword) || requester.includes(keyword) || room.includes(keyword);
                if (!matches) return false;
            }

            return true;
        });

        currentPage = 1;
        renderCurrentPage();
    };

    const renderCurrentPage = () => {
        taskTableBody.innerHTML = '';
        const total = filteredAssignments.length;

        if (total === 0) {
            techLoading.hidden = true;
            techEmpty.hidden = false;
            techContent.hidden = true;
            techPagination.hidden = true;
            return;
        }

        techLoading.hidden = true;
        techEmpty.hidden = true;
        techContent.hidden = false;

        const totalPages = Math.ceil(total / pageSize);
        if (currentPage > totalPages) currentPage = totalPages;
        const startIndex = (currentPage - 1) * pageSize;
        const pageItems = filteredAssignments.slice(startIndex, startIndex + pageSize);

        pageItems.forEach(item => {
            const req = item.request || {};
            const reqId = item.requestId || req.id;
            const reqCode = item.requestCode || req.requestCode || ('REQ-' + reqId);
            const status = item.requestStatus || req.status || 'ASSIGNED';
            const priority = item.requestPriority || req.priority || 'MEDIUM';

            const tr = document.createElement('tr');

            // 1. Mã YC
            const tdCode = document.createElement('td');
            const codeBadge = document.createElement('span');
            codeBadge.className = 'request-code-badge';
            codeBadge.textContent = reqCode;
            tdCode.appendChild(codeBadge);
            tr.appendChild(tdCode);

            // 2. Tiêu đề / Danh mục
            const tdTitle = document.createElement('td');
            const titleLink = document.createElement('a');
            titleLink.className = 'table-req-title';
            titleLink.href = `/requests/${reqId}`;
            titleLink.textContent = item.requestTitle || req.title || '—';
            titleLink.title = item.requestTitle || req.title || '';
            const subCategory = document.createElement('div');
            subCategory.className = 'table-req-category';
            subCategory.textContent = req.category?.name || 'Sự cố chung';
            tdTitle.appendChild(titleLink);
            tdTitle.appendChild(subCategory);
            tr.appendChild(tdTitle);

            // 3. Người gửi
            const tdRequester = document.createElement('td');
            tdRequester.textContent = item.requesterName || req.requester?.fullName || '—';
            tr.appendChild(tdRequester);

            // 4. Phòng / Vị trí
            const tdRoom = document.createElement('td');
            const roomTxt = item.roomName || (req.room ? (req.room.roomCode || req.room.roomName) : '');
            const devTxt = req.device ? req.device.deviceName : '';
            tdRoom.textContent = roomTxt ? (devTxt ? `${roomTxt} (${devTxt})` : roomTxt) : (devTxt || '—');
            tr.appendChild(tdRoom);

            // 5. Mức độ
            const tdPriority = document.createElement('td');
            const pBadge = document.createElement('span');
            const pKey = String(priority).toLowerCase();
            pBadge.className = `priority-badge priority-${pKey}`;
            pBadge.textContent = priorityLabels[priority] || priority;
            tdPriority.appendChild(pBadge);
            tr.appendChild(tdPriority);

            // 6. Trạng thái
            const tdStatus = document.createElement('td');
            const sBadge = document.createElement('span');
            const sKey = String(status).toLowerCase();
            sBadge.className = `request-status status-${sKey}`;
            sBadge.textContent = statusLabels[status] || status;
            tdStatus.appendChild(sBadge);
            tr.appendChild(tdStatus);

            // 7. Thời hạn SLA
            const tdSla = document.createElement('td');
            const slaDeadline = req.resolutionDeadline;
            const isOverdue = req.isOverdue || (slaDeadline && new Date(slaDeadline) < new Date() && status !== 'COMPLETED');
            if (isOverdue) {
                tdSla.innerHTML = `<div>${formatDateTime(slaDeadline)}</div><span class="sla-badge sla-overdue">Quá hạn</span>`;
            } else if (req.slaStatus === 'WARNING') {
                tdSla.innerHTML = `<div>${formatDateTime(slaDeadline)}</div><span class="sla-badge sla-warning">Sắp hết hạn</span>`;
            } else if (status === 'COMPLETED') {
                tdSla.innerHTML = `<span class="sla-badge sla-ok">Đã hoàn thành</span>`;
            } else {
                tdSla.innerHTML = `<div>${formatDateTime(slaDeadline)}</div><span class="sla-badge sla-ok">Trong hạn</span>`;
            }
            tr.appendChild(tdSla);

            // 8. Thao tác xử lý
            const tdActions = document.createElement('td');
            const group = document.createElement('div');
            group.className = 'btn-action-group';

            if (status === 'ASSIGNED') {
                const btnAccept = document.createElement('button');
                btnAccept.type = 'button';
                btnAccept.className = 'btn-action-sm btn-accept';
                btnAccept.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg> Nhận xử lý';
                btnAccept.addEventListener('click', () => handleAcceptRequest(reqId, btnAccept));
                group.appendChild(btnAccept);
            } else if (status === 'IN_PROGRESS') {
                const btnProgress = document.createElement('button');
                btnProgress.type = 'button';
                btnProgress.className = 'btn-action-sm btn-progress';
                btnProgress.innerHTML = 'Tiến độ';
                btnProgress.addEventListener('click', () => openActionModal('progress', reqId, 'Cập nhật tiến độ xử lý'));
                group.appendChild(btnProgress);

                const btnNeedInfo = document.createElement('button');
                btnNeedInfo.type = 'button';
                btnNeedInfo.className = 'btn-action-sm btn-need-info';
                btnNeedInfo.innerHTML = 'Cần TT';
                btnNeedInfo.addEventListener('click', () => openActionModal('need-info', reqId, 'Yêu cầu thêm thông tin từ người gửi'));
                group.appendChild(btnNeedInfo);

                const btnComplete = document.createElement('button');
                btnComplete.type = 'button';
                btnComplete.className = 'btn-action-sm btn-complete';
                btnComplete.innerHTML = 'Hoàn thành';
                btnComplete.addEventListener('click', () => openActionModal('complete', reqId, 'Hoàn thành xử lý yêu cầu'));
                group.appendChild(btnComplete);
            } else if (status === 'WAITING_INFO') {
                const btnResume = document.createElement('button');
                btnResume.type = 'button';
                btnResume.className = 'btn-action-sm btn-progress';
                btnResume.innerHTML = 'Tiếp tục';
                btnResume.addEventListener('click', () => openActionModal('resume', reqId, 'Tiếp tục xử lý sự cố'));
                group.appendChild(btnResume);
            }

            const btnView = document.createElement('a');
            btnView.className = 'btn-action-sm btn-view';
            btnView.href = `/requests/${reqId}`;
            btnView.textContent = 'Chi tiết';
            group.appendChild(btnView);

            tdActions.appendChild(group);
            tr.appendChild(tdActions);

            taskTableBody.appendChild(tr);
        });

        renderPagination(total, totalPages);
    };

    const renderPagination = (total, totalPages) => {
        if (totalPages <= 1) {
            techPagination.hidden = true;
            return;
        }

        techPagination.hidden = false;
        const start = (currentPage - 1) * pageSize + 1;
        const end = Math.min(currentPage * pageSize, total);
        techPaginationInfo.textContent = `Hiển thị ${start} - ${end} trên ${total} nhiệm vụ`;

        techPaginationControls.innerHTML = '';

        const prevBtn = document.createElement('button');
        prevBtn.className = 'pagination-btn';
        prevBtn.innerHTML = '‹';
        prevBtn.disabled = currentPage === 1;
        prevBtn.addEventListener('click', () => {
            if (currentPage > 1) {
                currentPage--;
                renderCurrentPage();
            }
        });
        techPaginationControls.appendChild(prevBtn);

        for (let i = 1; i <= totalPages; i++) {
            if (i === 1 || i === totalPages || (i >= currentPage - 2 && i <= currentPage + 2)) {
                const pageBtn = document.createElement('button');
                pageBtn.className = `pagination-btn ${i === currentPage ? 'active' : ''}`;
                pageBtn.textContent = i;
                pageBtn.addEventListener('click', () => {
                    currentPage = i;
                    renderCurrentPage();
                });
                techPaginationControls.appendChild(pageBtn);
            }
        }

        const nextBtn = document.createElement('button');
        nextBtn.className = 'pagination-btn';
        nextBtn.innerHTML = '›';
        nextBtn.disabled = currentPage === totalPages;
        nextBtn.addEventListener('click', () => {
            if (currentPage < totalPages) {
                currentPage++;
                renderCurrentPage();
            }
        });
        techPaginationControls.appendChild(nextBtn);
    };

    // Accept Request
    const handleAcceptRequest = async (requestId, btnEl) => {
        if (btnEl) {
            btnEl.disabled = true;
            btnEl.textContent = 'Đang nhận...';
        }

        try {
            const res = await fetch(`/api/requests/${requestId}/accept`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', Accept: 'application/json' }
            });
            const payload = await res.json();
            if (!res.ok || !payload.success) throw new Error(payload.message || 'Không thể tiếp nhận');

            if (window.showToast) window.showToast('success', 'Đã tiếp nhận', 'Bạn đã nhận xử lý yêu cầu này!');
            loadData();
        } catch (e) {
            if (window.showToast) window.showToast('error', 'Lỗi', e.message);
            else alert('Lỗi: ' + e.message);
            if (btnEl) btnEl.disabled = false;
        }
    };

    // Open Action Modal
    const openActionModal = (actionType, requestId, title) => {
        currentActionType = actionType;
        currentTargetRequestId = requestId;
        actionModalTitle.textContent = title;
        actionNoteInput.value = '';

        if (actionType === 'progress') {
            actionModalDesc.textContent = 'Ghi nhận tiến độ công việc đã thực hiện:';
            actionNoteInput.placeholder = 'Ví dụ: Đã kiểm tra dây cáp VGA/HDMI, chuẩn bị thay card màn hình...';
        } else if (actionType === 'need-info') {
            actionModalDesc.textContent = 'Yêu cầu người gửi cung cấp thêm thông tin chi tiết:';
            actionNoteInput.placeholder = 'Ví dụ: Vui lòng cho biết mã số máy tính hoặc lỗi hiển thị trên màn hình...';
        } else if (actionType === 'complete') {
            actionModalDesc.textContent = 'Xác nhận hoàn thành xử lý sự cố kỹ thuật:';
            actionNoteInput.placeholder = 'Ví dụ: Đã thay thế linh kiện hỏng, kiểm tra máy chiếu hoạt động ổn định...';
        } else if (actionType === 'resume') {
            actionModalDesc.textContent = 'Tiếp tục xử lý sau khi nhận thông tin:';
            actionNoteInput.placeholder = 'Ví dụ: Đã nhận được thông tin, tiến hành cài đặt lại phần mềm...';
        }

        actionModal.hidden = false;
        actionNoteInput.focus();
    };

    // Submit Action Modal
    btnActionModalSubmit.addEventListener('click', async () => {
        const note = actionNoteInput.value.trim();
        if (!note) {
            if (window.showToast) window.showToast('warning', 'Thiếu ghi chú', 'Vui lòng nhập ghi chú nội dung xử lý.');
            actionNoteInput.focus();
            return;
        }

        btnActionModalSubmit.disabled = true;
        btnActionModalSubmit.textContent = 'Đang lưu...';

        let url = '';
        if (currentActionType === 'progress') url = `/api/requests/${currentTargetRequestId}/progress`;
        else if (currentActionType === 'need-info') url = `/api/requests/${currentTargetRequestId}/need-info`;
        else if (currentActionType === 'complete') url = `/api/requests/${currentTargetRequestId}/complete`;
        else if (currentActionType === 'resume') url = `/api/requests/${currentTargetRequestId}/resume`;

        try {
            const res = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
                body: JSON.stringify({ note })
            });
            const payload = await res.json();
            if (!res.ok || !payload.success) throw new Error(payload.message || 'Thao tác thất bại');

            actionModal.hidden = true;
            if (window.showToast) window.showToast('success', 'Thành công', 'Đã cập nhật trạng thái nhiệm vụ!');
            loadData();
        } catch (e) {
            if (window.showToast) window.showToast('error', 'Lỗi', e.message);
            else alert('Lỗi: ' + e.message);
        } finally {
            btnActionModalSubmit.disabled = false;
            btnActionModalSubmit.textContent = 'Xác nhận';
        }
    });

    // Load Data
    const loadData = async () => {
        techLoading.hidden = false;
        techEmpty.hidden = true;
        techContent.hidden = true;
        techPagination.hidden = true;

        try {
            const meRes = await fetch('/api/auth/me');
            if (meRes.ok) {
                const meData = await meRes.json();
                if (meData.success && meData.data) currentUserId = meData.data.id;
            }

            const res = await fetch('/api/technician/requests', { headers: { Accept: 'application/json' } });
            const payload = await res.json();
            if (!res.ok || !payload.success) throw new Error(payload.message || 'Không thể tải dữ liệu');

            allAssignments = payload.data || [];
            updateCounts();
            applyFilters();
        } catch (e) {
            techLoading.hidden = true;
            techEmpty.hidden = false;
            console.error('Error fetching technician tasks', e);
        }
    };

    // Filter Chips Event
    filterChips.forEach(chip => {
        chip.addEventListener('click', () => {
            filterChips.forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            currentFilter = chip.dataset.filter;
            applyFilters();
        });
    });

    searchInput?.addEventListener('input', applyFilters);
    priorityFilter?.addEventListener('change', applyFilters);
    btnRefreshTasks?.addEventListener('click', loadData);

    loadData();
});
