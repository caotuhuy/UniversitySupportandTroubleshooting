// ============================================================
// EAUT SUPPORT — Requests Page Script
// Đại học Công nghệ Đông Á
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
    // DOM elements
    const searchInput = document.getElementById('requestSearch');
    const statusFilter = document.getElementById('statusFilter');
    const priorityFilter = document.getElementById('priorityFilter');
    const categoryFilter = document.getElementById('categoryFilter');
    const sortSelect = document.getElementById('sortSelect');
    const clearFiltersBtn = document.getElementById('clearFilters');
    const clearFiltersBtn2 = document.getElementById('clearFilters2');
    const retryBtn = document.getElementById('retryRequests');
    const tableBody = document.getElementById('requestTableBody');
    const requestCount = document.getElementById('requestCount');
    const requestError = document.getElementById('requestError');
    const requestSkeleton = document.getElementById('requestSkeleton');
    const requestEmpty = document.getElementById('requestEmpty');
    const requestTableWrap = document.getElementById('requestTableWrap');
    const requestPagination = document.getElementById('requestPagination');
    const paginationInfo = document.getElementById('paginationInfo');
    const paginationControls = document.getElementById('paginationControls');
    const filterChips = document.querySelectorAll('.requests-filter-chips .filter-chip');

    // State
    let allRequests = [];
    let filteredRequests = [];
    let activeChip = 'ALL';
    let currentPage = 1;
    const pageSize = 10;
    let searchDebounceTimer = null;

    const priorityLabels = {
        LOW: 'Thấp',
        MEDIUM: 'Trung bình',
        HIGH: 'Cao',
        URGENT: 'Khẩn cấp'
    };

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

    const priorityWeight = {
        URGENT: 4,
        HIGH: 3,
        MEDIUM: 2,
        LOW: 1
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

    // Load categories for filter
    const loadCategories = async () => {
        try {
            const res = await fetch('/api/categories', { headers: { Accept: 'application/json' } });
            if (!res.ok) return;
            const payload = await res.json();
            if (payload.success && Array.isArray(payload.data)) {
                categoryFilter.innerHTML = '<option value="">Tất cả danh mục</option>';
                payload.data.forEach(cat => {
                    const opt = document.createElement('option');
                    opt.value = cat.id;
                    opt.textContent = cat.name;
                    categoryFilter.appendChild(opt);
                });
            }
        } catch (e) {
            console.error('Failed to load categories', e);
        }
    };

    // Update chip counts
    const updateChipCounts = () => {
        const counts = {
            ALL: allRequests.length,
            PENDING: 0,
            PROCESSING: 0,
            COMPLETED: 0,
            URGENT: 0
        };

        allRequests.forEach(req => {
            const st = req.status;
            if (st === 'PENDING') counts.PENDING++;
            if (['RECEIVED', 'ASSIGNED', 'IN_PROGRESS', 'WAITING_INFO'].includes(st)) counts.PROCESSING++;
            if (st === 'COMPLETED') counts.COMPLETED++;
            if (req.priority === 'URGENT' && !['COMPLETED', 'CANCELLED', 'REJECTED'].includes(st)) counts.URGENT++;
        });

        document.getElementById('countChipAll').textContent = counts.ALL;
        document.getElementById('countChipPending').textContent = counts.PENDING;
        document.getElementById('countChipProcessing').textContent = counts.PROCESSING;
        document.getElementById('countChipCompleted').textContent = counts.COMPLETED;
        document.getElementById('countChipUrgent').textContent = counts.URGENT;
    };

    // Apply filters and sorting
    const applyFiltersAndSort = () => {
        const keyword = (searchInput.value || '').trim().toLowerCase();
        const statusVal = statusFilter.value;
        const priorityVal = priorityFilter.value;
        const categoryVal = categoryFilter.value;
        const sortVal = sortSelect.value;

        filteredRequests = allRequests.filter(req => {
            // Chip filter
            if (activeChip === 'PENDING' && req.status !== 'PENDING') return false;
            if (activeChip === 'PROCESSING' && !['RECEIVED', 'ASSIGNED', 'IN_PROGRESS', 'WAITING_INFO'].includes(req.status)) return false;
            if (activeChip === 'COMPLETED' && req.status !== 'COMPLETED') return false;
            if (activeChip === 'URGENT' && (req.priority !== 'URGENT' || ['COMPLETED', 'CANCELLED', 'REJECTED'].includes(req.status))) return false;

            // Status select
            if (statusVal && req.status !== statusVal) return false;

            // Priority select
            if (priorityVal && req.priority !== priorityVal) return false;

            // Category select
            if (categoryVal && (!req.category || String(req.category.id) !== categoryVal)) return false;

            // Keyword search
            if (keyword) {
                const code = (req.requestCode || '').toLowerCase();
                const title = (req.title || '').toLowerCase();
                const desc = (req.description || '').toLowerCase();
                const requester = (req.requester?.fullName || '').toLowerCase();
                const room = (req.room?.roomName || req.room?.roomCode || '').toLowerCase();
                const catName = (req.category?.name || '').toLowerCase();
                const matches = code.includes(keyword) || title.includes(keyword) ||
                                desc.includes(keyword) || requester.includes(keyword) ||
                                room.includes(keyword) || catName.includes(keyword);
                if (!matches) return false;
            }

            return true;
        });

        // Sorting
        filteredRequests.sort((a, b) => {
            if (sortVal === 'newest') {
                return new Date(b.createdAt || 0) - new Date(a.createdAt || 0);
            } else if (sortVal === 'oldest') {
                return new Date(a.createdAt || 0) - new Date(b.createdAt || 0);
            } else if (sortVal === 'priority') {
                const wa = priorityWeight[a.priority] || 0;
                const wb = priorityWeight[b.priority] || 0;
                if (wb !== wa) return wb - wa;
                return new Date(b.createdAt || 0) - new Date(a.createdAt || 0);
            }
            return 0;
        });

        currentPage = 1;
        renderCurrentPage();
    };

    // Render table rows for current page
    const renderCurrentPage = () => {
        const total = filteredRequests.length;
        requestCount.textContent = `${total.toLocaleString('vi-VN')} yêu cầu`;

        if (total === 0) {
            requestSkeleton.hidden = true;
            requestTableWrap.hidden = true;
            requestPagination.hidden = true;
            requestEmpty.hidden = false;
            return;
        }

        requestEmpty.hidden = true;
        requestSkeleton.hidden = true;
        requestTableWrap.hidden = false;

        const totalPages = Math.ceil(total / pageSize);
        if (currentPage > totalPages) currentPage = totalPages;
        const startIndex = (currentPage - 1) * pageSize;
        const pageItems = filteredRequests.slice(startIndex, startIndex + pageSize);

        tableBody.innerHTML = '';
        pageItems.forEach(req => {
            const tr = document.createElement('tr');

            // 1. Mã YC
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

            // 4. Phòng
            const tdRoom = document.createElement('td');
            const roomTxt = req.room ? (req.room.roomCode || req.room.roomName) : '—';
            tdRoom.textContent = roomTxt;
            tr.appendChild(tdRoom);

            // 5. Mức độ
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
            const slaBadge = document.createElement('span');
            if (req.isOverdue) {
                slaBadge.className = 'sla-badge sla-overdue';
                slaBadge.textContent = 'Quá hạn';
            } else if (req.slaStatus === 'WARNING' || req.slaStatusText?.includes('Cảnh báo')) {
                slaBadge.className = 'sla-badge sla-warning';
                slaBadge.textContent = 'Sắp quá hạn';
            } else if (req.status === 'COMPLETED') {
                slaBadge.className = 'sla-badge sla-ok';
                slaBadge.textContent = 'Đã hoàn thành';
            } else {
                slaBadge.className = 'sla-badge sla-ok';
                slaBadge.textContent = 'Trong hạn';
            }
            tdSla.appendChild(slaBadge);
            tr.appendChild(tdSla);

            // 8. Ngày tạo
            const tdDate = document.createElement('td');
            tdDate.style.fontSize = 'var(--text-xs)';
            tdDate.style.color = 'var(--text-muted)';
            tdDate.textContent = formatDateTime(req.createdAt);
            tr.appendChild(tdDate);

            // 9. Thao tác (Xem)
            const tdAction = document.createElement('td');
            tdAction.className = 'col-actions';
            const viewBtn = document.createElement('a');
            viewBtn.className = 'btn btn-sm btn-ghost';
            viewBtn.href = `/requests/${req.id}`;
            viewBtn.textContent = 'Xem';
            tdAction.appendChild(viewBtn);
            tr.appendChild(tdAction);

            tableBody.appendChild(tr);
        });

        renderPagination(total, totalPages);
    };

    // Render pagination controls
    const renderPagination = (total, totalPages) => {
        if (totalPages <= 1) {
            requestPagination.hidden = true;
            return;
        }

        requestPagination.hidden = false;
        const start = (currentPage - 1) * pageSize + 1;
        const end = Math.min(currentPage * pageSize, total);
        paginationInfo.textContent = `Hiển thị ${start} - ${end} trên ${total} yêu cầu`;

        paginationControls.innerHTML = '';

        // Prev button
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
        paginationControls.appendChild(prevBtn);

        // Page buttons
        for (let i = 1; i <= totalPages; i++) {
            if (i === 1 || i === totalPages || (i >= currentPage - 2 && i <= currentPage + 2)) {
                const pageBtn = document.createElement('button');
                pageBtn.className = `pagination-btn ${i === currentPage ? 'active' : ''}`;
                pageBtn.textContent = i;
                pageBtn.addEventListener('click', () => {
                    currentPage = i;
                    renderCurrentPage();
                });
                paginationControls.appendChild(pageBtn);
            } else if (i === currentPage - 3 || i === currentPage + 3) {
                const dots = document.createElement('span');
                dots.style.padding = '0 4px';
                dots.style.color = 'var(--text-muted)';
                dots.textContent = '...';
                paginationControls.appendChild(dots);
            }
        }

        // Next button
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
        paginationControls.appendChild(nextBtn);
    };

    // Load data from API
    const loadRequests = async () => {
        requestError.hidden = true;
        requestSkeleton.hidden = false;
        requestTableWrap.hidden = true;
        requestEmpty.hidden = true;
        requestPagination.hidden = true;
        requestCount.textContent = 'Đang tải...';

        try {
            const res = await fetch('/api/requests', {
                headers: { Accept: 'application/json' }
            });
            const payload = await res.json();
            if (!res.ok || !payload.success || !Array.isArray(payload.data)) {
                throw new Error(payload.message || 'Không thể lấy danh sách yêu cầu');
            }

            allRequests = payload.data;
            updateChipCounts();
            applyFiltersAndSort();
        } catch (error) {
            requestSkeleton.hidden = true;
            requestTableWrap.hidden = true;
            requestEmpty.hidden = true;
            requestPagination.hidden = true;
            requestError.hidden = false;
            requestCount.textContent = 'Lỗi tải dữ liệu';
            console.error('Error fetching requests:', error);
        }
    };

    // Filter Chips Event
    filterChips.forEach(chip => {
        chip.addEventListener('click', () => {
            filterChips.forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            activeChip = chip.dataset.chip;
            applyFiltersAndSort();
        });
    });

    // Search input with debounce
    searchInput.addEventListener('input', () => {
        clearTimeout(searchDebounceTimer);
        searchDebounceTimer = setTimeout(applyFiltersAndSort, 250);
    });

    // Select filters
    statusFilter.addEventListener('change', applyFiltersAndSort);
    priorityFilter.addEventListener('change', applyFiltersAndSort);
    categoryFilter.addEventListener('change', applyFiltersAndSort);
    sortSelect.addEventListener('change', applyFiltersAndSort);

    // Reset filters
    const resetAllFilters = () => {
        searchInput.value = '';
        statusFilter.value = '';
        priorityFilter.value = '';
        categoryFilter.value = '';
        sortSelect.value = 'newest';
        activeChip = 'ALL';
        filterChips.forEach(c => c.classList.remove('active'));
        document.querySelector('.filter-chip[data-chip="ALL"]')?.classList.add('active');
        applyFiltersAndSort();
    };

    clearFiltersBtn?.addEventListener('click', resetAllFilters);
    clearFiltersBtn2?.addEventListener('click', resetAllFilters);
    retryBtn?.addEventListener('click', loadRequests);

    // Initial load
    loadCategories();
    loadRequests();
});
