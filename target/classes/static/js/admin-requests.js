// ============================================================
// EAUT SUPPORT — Admin Request Management Script
// Đại học Công nghệ Đông Á
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
    let currentUserId = null;
    let allRequests = [];
    let filteredRequests = [];
    let technicians = [];
    let categories = [];
    let currentFilter = 'PENDING';
    let currentTargetRequestId = null;
    let isReassignMode = false;
    let currentPage = 1;
    const pageSize = 10;

    // DOM Elements
    const adminLoading = document.getElementById('adminLoading');
    const adminEmpty = document.getElementById('adminEmpty');
    const adminContent = document.getElementById('adminContent');
    const adminTableBody = document.getElementById('adminTableBody');
    const adminAlert = document.getElementById('adminAlert');
    const filterChips = document.querySelectorAll('.filter-chips .filter-chip');
    const adminSearchInput = document.getElementById('adminSearchInput');
    const adminPriorityFilter = document.getElementById('adminPriorityFilter');
    const adminCategoryFilter = document.getElementById('adminCategoryFilter');
    const btnRefresh = document.getElementById('btnRefreshAdminRequests');

    // KPI Elements
    const kpiPending = document.getElementById('kpiPending');
    const kpiReceived = document.getElementById('kpiReceived');
    const kpiProcessing = document.getElementById('kpiProcessing');
    const kpiCompleted = document.getElementById('kpiCompleted');
    const kpiOverdue = document.getElementById('kpiOverdue');

    // Pagination
    const adminPagination = document.getElementById('adminPagination');
    const adminPaginationInfo = document.getElementById('adminPaginationInfo');
    const adminPaginationControls = document.getElementById('adminPaginationControls');

    // Modals
    const assignModal = document.getElementById('assignModal');
    const assignModalTitle = document.getElementById('assignModalTitle');
    const techSelect = document.getElementById('techSelect');
    const assignNoteInput = document.getElementById('assignNoteInput');
    const btnAssignModalSubmit = document.getElementById('btnAssignModalSubmit');

    const rejectModal = document.getElementById('rejectModal');
    const rejectReasonInput = document.getElementById('rejectReasonInput');
    const btnRejectModalSubmit = document.getElementById('btnRejectModalSubmit');

    const classifyModal = document.getElementById('classifyModal');
    const classifyCategorySelect = document.getElementById('classifyCategorySelect');
    const classifyPrioritySelect = document.getElementById('classifyPrioritySelect');
    const btnClassifyModalSubmit = document.getElementById('btnClassifyModalSubmit');

    const deleteModal = document.getElementById('deleteModal');
    const deleteModalConfirmText = document.getElementById('deleteModalConfirmText');
    const btnConfirmDelete = document.getElementById('btnConfirmDelete');

    const statusLabels = {
        PENDING: 'Chờ tiếp nhận',
        RECEIVED: 'Đã tiếp nhận',
        ASSIGNED: 'Đã phân công',
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
            PENDING: 0,
            RECEIVED: 0,
            PROCESSING: 0,
            COMPLETED: 0,
            OVERDUE: 0,
            ALL: allRequests.length
        };

        allRequests.forEach(r => {
            const st = r.status;
            if (st === 'PENDING') counts.PENDING++;
            else if (st === 'RECEIVED') counts.RECEIVED++;
            else if (['ASSIGNED', 'IN_PROGRESS', 'WAITING_INFO'].includes(st)) counts.PROCESSING++;
            else if (st === 'COMPLETED') counts.COMPLETED++;

            if (r.isOverdue || r.slaStatus === 'OVERDUE' || (r.resolutionDeadline && new Date(r.resolutionDeadline) < new Date() && st !== 'COMPLETED')) {
                counts.OVERDUE++;
            }
        });

        // Filter chips counts
        document.getElementById('countPending').textContent = counts.PENDING;
        document.getElementById('countReceived').textContent = counts.RECEIVED;
        document.getElementById('countProcessing').textContent = counts.PROCESSING;
        document.getElementById('countCompleted').textContent = counts.COMPLETED;
        document.getElementById('countAll').textContent = counts.ALL;

        // KPI metric cards
        if (kpiPending) kpiPending.textContent = counts.PENDING;
        if (kpiReceived) kpiReceived.textContent = counts.RECEIVED;
        if (kpiProcessing) kpiProcessing.textContent = counts.PROCESSING;
        if (kpiCompleted) kpiCompleted.textContent = counts.COMPLETED;
        if (kpiOverdue) kpiOverdue.textContent = counts.OVERDUE;
    };

    const applyFilters = () => {
        const keyword = (adminSearchInput?.value || '').trim().toLowerCase();
        const priorityVal = adminPriorityFilter?.value || '';
        const categoryVal = adminCategoryFilter?.value || '';

        filteredRequests = allRequests.filter(req => {
            // Chip filter
            if (currentFilter === 'PENDING' && req.status !== 'PENDING') return false;
            if (currentFilter === 'RECEIVED' && req.status !== 'RECEIVED') return false;
            if (currentFilter === 'IN_PROGRESS' && !['ASSIGNED', 'IN_PROGRESS', 'WAITING_INFO'].includes(req.status)) return false;
            if (currentFilter === 'COMPLETED' && req.status !== 'COMPLETED') return false;

            // Priority select
            if (priorityVal && req.priority !== priorityVal) return false;

            // Category select
            if (categoryVal && (!req.category || String(req.category.id) !== categoryVal)) return false;

            // Keyword search
            if (keyword) {
                const code = (req.requestCode || '').toLowerCase();
                const title = (req.title || '').toLowerCase();
                const requester = (req.requester?.fullName || '').toLowerCase();
                const room = (req.room?.roomName || req.room?.roomCode || '').toLowerCase();
                const catName = (req.category?.name || '').toLowerCase();
                const matches = code.includes(keyword) || title.includes(keyword) || requester.includes(keyword) || room.includes(keyword) || catName.includes(keyword);
                if (!matches) return false;
            }

            return true;
        });

        currentPage = 1;
        renderCurrentPage();
    };

    const renderCurrentPage = () => {
        adminTableBody.innerHTML = '';
        const total = filteredRequests.length;

        if (total === 0) {
            adminLoading.hidden = true;
            adminEmpty.hidden = false;
            adminContent.hidden = true;
            adminPagination.hidden = true;
            return;
        }

        adminLoading.hidden = true;
        adminEmpty.hidden = true;
        adminContent.hidden = false;

        const totalPages = Math.ceil(total / pageSize);
        if (currentPage > totalPages) currentPage = totalPages;
        const startIndex = (currentPage - 1) * pageSize;
        const pageItems = filteredRequests.slice(startIndex, startIndex + pageSize);

        pageItems.forEach(req => {
            const tr = document.createElement('tr');

            // 1. Mã yêu cầu
            const tdCode = document.createElement('td');
            const codeBadge = document.createElement('span');
            codeBadge.className = 'request-code-badge';
            codeBadge.textContent = req.requestCode || ('REQ-' + req.id);
            tdCode.appendChild(codeBadge);
            tr.appendChild(tdCode);

            // 2. Tiêu đề / Danh mục
            const tdTitle = document.createElement('td');
            const titleLink = document.createElement('a');
            titleLink.className = 'table-req-title';
            titleLink.href = `/requests/${req.id}`;
            titleLink.textContent = req.title || '—';
            titleLink.title = req.title || '';
            const subCategory = document.createElement('div');
            subCategory.className = 'table-req-category';
            subCategory.textContent = req.category?.name || 'Chưa phân loại';
            tdTitle.appendChild(titleLink);
            tdTitle.appendChild(subCategory);
            tr.appendChild(tdTitle);

            // 3. Người gửi
            const tdRequester = document.createElement('td');
            tdRequester.textContent = req.requester?.fullName || '—';
            tr.appendChild(tdRequester);

            // 4. Vị trí
            const tdLocation = document.createElement('td');
            const roomTxt = req.room ? (req.room.roomCode || req.room.roomName) : '';
            const devTxt = req.device ? (req.device.deviceName || req.device.deviceCode) : '';
            tdLocation.textContent = roomTxt ? (devTxt ? `${roomTxt} (${devTxt})` : roomTxt) : (devTxt || '—');
            tr.appendChild(tdLocation);

            // 5. Ưu tiên
            const tdPriority = document.createElement('td');
            const pBadge = document.createElement('span');
            const pKey = String(req.priority || 'MEDIUM').toLowerCase();
            pBadge.className = `priority-badge priority-${pKey}`;
            pBadge.textContent = priorityLabels[req.priority] || req.priority;
            tdPriority.appendChild(pBadge);
            tr.appendChild(tdPriority);

            // 6. Trạng thái
            const tdStatus = document.createElement('td');
            const sBadge = document.createElement('span');
            const sKey = String(req.status || 'PENDING').toLowerCase();
            sBadge.className = `request-status status-${sKey}`;
            sBadge.textContent = statusLabels[req.status] || req.status;
            tdStatus.appendChild(sBadge);
            tr.appendChild(tdStatus);

            // 7. SLA
            const tdSla = document.createElement('td');
            if (req.isOverdue) {
                tdSla.innerHTML = '<span class="sla-badge sla-overdue">Quá hạn</span>';
            } else if (req.slaStatus === 'WARNING') {
                tdSla.innerHTML = '<span class="sla-badge sla-warning">Sắp hết hạn</span>';
            } else if (req.status === 'COMPLETED') {
                tdSla.innerHTML = '<span class="sla-badge sla-ok">Hoàn thành</span>';
            } else {
                tdSla.innerHTML = '<span class="sla-badge sla-ok">Trong hạn</span>';
            }
            tr.appendChild(tdSla);

            // 8. Ngày gửi
            const tdTime = document.createElement('td');
            tdTime.style.fontSize = 'var(--text-xs)';
            tdTime.style.color = 'var(--text-muted)';
            tdTime.textContent = formatDateTime(req.createdAt);
            tr.appendChild(tdTime);

            // 9. Hành động
            const tdActions = document.createElement('td');
            const group = document.createElement('div');
            group.className = 'btn-action-group';

            if (req.status === 'PENDING') {
                const btnReceive = document.createElement('button');
                btnReceive.type = 'button';
                btnReceive.className = 'btn-action-sm btn-receive';
                btnReceive.textContent = 'Tiếp nhận';
                btnReceive.addEventListener('click', () => handleReceiveRequest(req.id, btnReceive));
                group.appendChild(btnReceive);

                const btnReject = document.createElement('button');
                btnReject.type = 'button';
                btnReject.className = 'btn-action-sm btn-reject';
                btnReject.textContent = 'Từ chối';
                btnReject.addEventListener('click', () => openRejectModal(req.id));
                group.appendChild(btnReject);
            }

            if (req.status === 'RECEIVED') {
                const btnAssign = document.createElement('button');
                btnAssign.type = 'button';
                btnAssign.className = 'btn-action-sm btn-assign';
                btnAssign.textContent = 'Phân công';
                btnAssign.addEventListener('click', () => openAssignModal(req.id, false));
                group.appendChild(btnAssign);
            } else if (['ASSIGNED', 'IN_PROGRESS', 'WAITING_INFO'].includes(req.status)) {
                const btnReassign = document.createElement('button');
                btnReassign.type = 'button';
                btnReassign.className = 'btn-action-sm btn-reassign';
                btnReassign.textContent = 'Đổi KTV';
                btnReassign.addEventListener('click', () => openAssignModal(req.id, true));
                group.appendChild(btnReassign);
            }

            if (!['COMPLETED', 'CANCELLED', 'REJECTED'].includes(req.status)) {
                const btnClassify = document.createElement('button');
                btnClassify.type = 'button';
                btnClassify.className = 'btn-action-sm btn-classify';
                btnClassify.textContent = 'Phân loại';
                btnClassify.addEventListener('click', () => openClassifyModal(req));
                group.appendChild(btnClassify);
            }

            const btnView = document.createElement('a');
            btnView.className = 'btn-action-sm btn-view';
            btnView.href = `/requests/${req.id}`;
            btnView.textContent = 'Xem';
            group.appendChild(btnView);

            const btnDel = document.createElement('button');
            btnDel.type = 'button';
            btnDel.className = 'btn-action-sm btn-delete';
            btnDel.title = 'Xóa yêu cầu';
            btnDel.textContent = 'Xóa';
            btnDel.addEventListener('click', () => openDeleteModal(req));
            group.appendChild(btnDel);

            tdActions.appendChild(group);
            tr.appendChild(tdActions);

            adminTableBody.appendChild(tr);
        });

        renderPagination(total, totalPages);
    };

    const renderPagination = (total, totalPages) => {
        if (totalPages <= 1) {
            adminPagination.hidden = true;
            return;
        }

        adminPagination.hidden = false;
        const start = (currentPage - 1) * pageSize + 1;
        const end = Math.min(currentPage * pageSize, total);
        adminPaginationInfo.textContent = `Hiển thị ${start} - ${end} trên ${total} yêu cầu`;

        adminPaginationControls.innerHTML = '';

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
        adminPaginationControls.appendChild(prevBtn);

        for (let i = 1; i <= totalPages; i++) {
            if (i === 1 || i === totalPages || (i >= currentPage - 2 && i <= currentPage + 2)) {
                const pageBtn = document.createElement('button');
                pageBtn.className = `pagination-btn ${i === currentPage ? 'active' : ''}`;
                pageBtn.textContent = i;
                pageBtn.addEventListener('click', () => {
                    currentPage = i;
                    renderCurrentPage();
                });
                adminPaginationControls.appendChild(pageBtn);
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
        adminPaginationControls.appendChild(nextBtn);
    };

    // Receive Request
    const handleReceiveRequest = async (requestId, btnEl) => {
        if (btnEl) {
            btnEl.disabled = true;
            btnEl.textContent = 'Đang tiếp nhận...';
        }

        try {
            const res = await fetch(`/api/requests/${requestId}/receive`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', Accept: 'application/json' }
            });
            const payload = await res.json();
            if (!res.ok || !payload.success) throw new Error(payload.message || 'Không thể tiếp nhận');

            if (window.showToast) window.showToast('success', 'Đã tiếp nhận', 'Yêu cầu đã được tiếp nhận thành công!');
            loadData();
        } catch (e) {
            if (window.showToast) window.showToast('error', 'Lỗi', e.message);
            else alert('Lỗi: ' + e.message);
            if (btnEl) btnEl.disabled = false;
        }
    };

    // Assign / Reassign Modal
    const openAssignModal = (requestId, isReassign) => {
        currentTargetRequestId = requestId;
        isReassignMode = isReassign;
        assignModalTitle.textContent = isReassign ? 'Tái phân công kỹ thuật viên' : 'Phân công kỹ thuật viên';
        btnAssignModalSubmit.textContent = isReassign ? 'Xác nhận đổi KTV' : 'Xác nhận phân công';
        assignNoteInput.value = '';

        techSelect.innerHTML = '<option value="">-- Chọn kỹ thuật viên phụ trách --</option>';
        technicians.forEach(t => {
            const opt = document.createElement('option');
            opt.value = t.id;
            opt.textContent = `${t.fullName} (${t.username})${t.phone ? ' - SĐT: ' + t.phone : ''}`;
            techSelect.appendChild(opt);
        });

        assignModal.hidden = false;
    };

    btnAssignModalSubmit.addEventListener('click', async () => {
        const technicianId = techSelect.value;
        if (!technicianId) {
            if (window.showToast) window.showToast('warning', 'Chưa chọn KTV', 'Vui lòng chọn kỹ thuật viên để phân công.');
            techSelect.focus();
            return;
        }

        const note = assignNoteInput.value.trim();
        btnAssignModalSubmit.disabled = true;
        btnAssignModalSubmit.textContent = 'Đang phân công...';

        const endpoint = isReassignMode
            ? `/api/requests/${currentTargetRequestId}/reassign`
            : `/api/requests/${currentTargetRequestId}/assign`;

        const body = isReassignMode
            ? { newTechnicianId: Number(technicianId), note }
            : { technicianId: Number(technicianId), note };

        try {
            const res = await fetch(endpoint, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
                body: JSON.stringify(body)
            });
            const payload = await res.json();
            if (!res.ok || !payload.success) throw new Error(payload.message || 'Phân công thất bại');

            assignModal.hidden = true;
            if (window.showToast) window.showToast('success', 'Thành công', isReassignMode ? 'Đã đổi kỹ thuật viên thành công!' : 'Đã phân công kỹ thuật viên xử lý yêu cầu!');
            loadData();
        } catch (e) {
            if (window.showToast) window.showToast('error', 'Lỗi', e.message);
            else alert('Lỗi: ' + e.message);
        } finally {
            btnAssignModalSubmit.disabled = false;
            btnAssignModalSubmit.textContent = isReassignMode ? 'Xác nhận đổi KTV' : 'Xác nhận phân công';
        }
    });

    // Reject Modal
    const openRejectModal = (requestId) => {
        currentTargetRequestId = requestId;
        rejectReasonInput.value = '';
        rejectModal.hidden = false;
        rejectReasonInput.focus();
    };

    btnRejectModalSubmit.addEventListener('click', async () => {
        const reason = rejectReasonInput.value.trim();
        if (!reason) {
            if (window.showToast) window.showToast('warning', 'Chưa nhập lý do', 'Vui lòng nhập lý do từ chối yêu cầu.');
            rejectReasonInput.focus();
            return;
        }

        btnRejectModalSubmit.disabled = true;
        btnRejectModalSubmit.textContent = 'Đang từ chối...';

        try {
            const res = await fetch(`/api/requests/${currentTargetRequestId}/reject`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
                body: JSON.stringify({ reason })
            });
            const payload = await res.json();
            if (!res.ok || !payload.success) throw new Error(payload.message || 'Từ chối thất bại');

            rejectModal.hidden = true;
            if (window.showToast) window.showToast('success', 'Đã từ chối', 'Đã từ chối yêu cầu hỗ trợ.');
            loadData();
        } catch (e) {
            if (window.showToast) window.showToast('error', 'Lỗi', e.message);
            else alert('Lỗi: ' + e.message);
        } finally {
            btnRejectModalSubmit.disabled = false;
            btnRejectModalSubmit.textContent = 'Xác nhận từ chối';
        }
    });

    // Classify Modal
    const openClassifyModal = (req) => {
        currentTargetRequestId = req.id;
        classifyCategorySelect.innerHTML = '';
        categories.forEach(cat => {
            const opt = document.createElement('option');
            opt.value = cat.id;
            opt.textContent = cat.name;
            if (req.category && req.category.id === cat.id) opt.selected = true;
            classifyCategorySelect.appendChild(opt);
        });

        classifyPrioritySelect.value = req.priority || 'MEDIUM';
        classifyModal.hidden = false;
    };

    btnClassifyModalSubmit.addEventListener('click', async () => {
        const categoryId = classifyCategorySelect.value;
        const priority = classifyPrioritySelect.value;

        btnClassifyModalSubmit.disabled = true;
        btnClassifyModalSubmit.textContent = 'Đang lưu...';

        try {
            if (categoryId) {
                await fetch(`/api/requests/${currentTargetRequestId}/category`, {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
                    body: JSON.stringify({ categoryId: Number(categoryId) })
                });
            }

            if (priority) {
                await fetch(`/api/requests/${currentTargetRequestId}/priority`, {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
                    body: JSON.stringify({ priority })
                });
            }

            classifyModal.hidden = true;
            if (window.showToast) window.showToast('success', 'Thành công', 'Đã cập nhật phân loại và độ ưu tiên!');
            loadData();
        } catch (e) {
            if (window.showToast) window.showToast('error', 'Lỗi', e.message);
            else alert('Lỗi: ' + e.message);
        } finally {
            btnClassifyModalSubmit.disabled = false;
            btnClassifyModalSubmit.textContent = 'Lưu thay đổi';
        }
    });

    // Delete Modal
    const openDeleteModal = (req) => {
        currentTargetRequestId = req.id;
        deleteModalConfirmText.textContent = `Bạn có chắc chắn muốn xóa yêu cầu "${req.requestCode || ('REQ-' + req.id)}" (${req.title})?`;
        deleteModal.hidden = false;
    };

    btnConfirmDelete.addEventListener('click', async () => {
        btnConfirmDelete.disabled = true;
        btnConfirmDelete.textContent = 'Đang xóa...';

        try {
            const res = await fetch(`/api/requests/${currentTargetRequestId}`, {
                method: 'DELETE',
                headers: { Accept: 'application/json' }
            });
            const payload = await res.json();
            if (!res.ok || !payload.success) throw new Error(payload.message || 'Xóa thất bại');

            deleteModal.hidden = true;
            if (window.showToast) window.showToast('success', 'Đã xóa', 'Yêu cầu hỗ trợ đã được xóa khỏi hệ thống.');
            loadData();
        } catch (e) {
            if (window.showToast) window.showToast('error', 'Lỗi', e.message);
            else alert('Lỗi: ' + e.message);
        } finally {
            btnConfirmDelete.disabled = false;
            btnConfirmDelete.textContent = 'Xóa vĩnh viễn';
        }
    });

    // Load Data
    const loadData = async () => {
        adminLoading.hidden = false;
        adminEmpty.hidden = true;
        adminContent.hidden = true;
        adminPagination.hidden = true;

        try {
            const [meRes, usersRes, catRes, reqRes] = await Promise.all([
                fetch('/api/auth/me'),
                fetch('/api/users'),
                fetch('/api/categories'),
                fetch('/api/requests')
            ]);

            if (meRes.ok) {
                const meData = await meRes.json();
                if (meData.success && meData.data) currentUserId = meData.data.id;
            }

            if (usersRes.ok) {
                const uData = await usersRes.json();
                if (uData.success && Array.isArray(uData.data)) {
                    technicians = uData.data.filter(u => u.roles && u.roles.some(r => r.name === 'ROLE_TECHNICIAN'));
                }
            }

            if (catRes.ok) {
                const cData = await catRes.json();
                if (cData.success && Array.isArray(cData.data)) {
                    categories = cData.data;
                    adminCategoryFilter.innerHTML = '<option value="">Tất cả danh mục</option>';
                    categories.forEach(cat => {
                        const opt = document.createElement('option');
                        opt.value = cat.id;
                        opt.textContent = cat.name;
                        adminCategoryFilter.appendChild(opt);
                    });
                }
            }

            if (reqRes.ok) {
                const rData = await reqRes.json();
                if (rData.success && Array.isArray(rData.data)) {
                    allRequests = rData.data;
                    updateCounts();
                    applyFilters();
                }
            }
        } catch (e) {
            adminLoading.hidden = true;
            adminEmpty.hidden = false;
            console.error('Error fetching admin requests', e);
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

    adminSearchInput?.addEventListener('input', applyFilters);
    adminPriorityFilter?.addEventListener('change', applyFilters);
    adminCategoryFilter?.addEventListener('change', applyFilters);
    btnRefresh?.addEventListener('click', loadData);

    loadData();
});
