// JavaScript cho trang Cấu hình SLA của Admin
document.addEventListener('DOMContentLoaded', () => {
    let allSlaConfigs = [];

    const slaLoading = document.getElementById('slaLoading');
    const slaEmpty = document.getElementById('slaEmpty');
    const slaContent = document.getElementById('slaContent');
    const slaTableBody = document.getElementById('slaTableBody');
    const slaAlert = document.getElementById('slaAlert');

    const slaModal = document.getElementById('slaModal');
    const slaModalTitle = document.getElementById('slaModalTitle');
    const slaForm = document.getElementById('slaForm');
    const slaIdInput = document.getElementById('slaIdInput');
    const inputSlaPriority = document.getElementById('inputSlaPriority');
    const inputResponseTime = document.getElementById('inputResponseTime');
    const inputResolutionTime = document.getElementById('inputResolutionTime');
    const inputWarningTime = document.getElementById('inputWarningTime');
    const inputSlaStatus = document.getElementById('inputSlaStatus');

    const btnOpenCreate = document.getElementById('btnOpenCreateSla');
    const btnCloseModal = document.getElementById('btnSlaModalClose');

    const priorityLabels = {
        LOW: 'Thấp (LOW)',
        MEDIUM: 'Trung bình (MEDIUM)',
        HIGH: 'Cao (HIGH)',
        URGENT: 'Khẩn cấp (URGENT)'
    };

    const showAlert = (msg, type = 'success') => {
        if (!slaAlert) return;
        slaAlert.className = `tech-alert ${type}`;
        slaAlert.textContent = msg;
        slaAlert.style.display = 'block';
        window.scrollTo({ top: 0, behavior: 'smooth' });
        setTimeout(() => { slaAlert.style.display = 'none'; }, 4000);
    };

    const renderTable = () => {
        slaTableBody.innerHTML = '';

        if (allSlaConfigs.length === 0) {
            slaEmpty.hidden = false;
            slaContent.hidden = true;
            return;
        }

        slaEmpty.hidden = true;
        slaContent.hidden = false;

        allSlaConfigs.forEach(s => {
            const tr = document.createElement('tr');

            // ID
            const tdId = document.createElement('td');
            tdId.textContent = s.id;
            tr.appendChild(tdId);

            // Priority
            const tdPriority = document.createElement('td');
            const pBadge = document.createElement('span');
            pBadge.className = `priority-badge priority-${String(s.priority).toLowerCase()}`;
            pBadge.textContent = priorityLabels[s.priority] || s.priority;
            tdPriority.appendChild(pBadge);
            tr.appendChild(tdPriority);

            // Response Time
            const tdResponse = document.createElement('td');
            tdResponse.textContent = `${s.responseTimeHours || s.responseTime || 0} giờ`;
            tr.appendChild(tdResponse);

            // Resolution Time
            const tdResolution = document.createElement('td');
            tdResolution.textContent = `${s.resolutionTimeHours || s.resolutionTime || 0} giờ`;
            tr.appendChild(tdResolution);

            // Warning Time
            const tdWarning = document.createElement('td');
            tdWarning.textContent = `${s.warningTimeHours || s.warningTime || 0} giờ`;
            tr.appendChild(tdWarning);

            // Status
            const tdStatus = document.createElement('td');
            const sBadge = document.createElement('span');
            const isActive = s.status === 'ACTIVE';
            sBadge.className = isActive ? 'request-status status-completed' : 'request-status status-cancelled';
            sBadge.textContent = isActive ? 'Hoạt động' : 'Tạm ngưng';
            tdStatus.appendChild(sBadge);
            tr.appendChild(tdStatus);

            // Actions
            const tdActions = document.createElement('td');
            const group = document.createElement('div');
            group.className = 'btn-action-group';

            const btnEdit = document.createElement('button');
            btnEdit.className = 'btn-action-sm btn-view';
            btnEdit.textContent = 'Sửa';
            btnEdit.addEventListener('click', () => openEditModal(s));
            group.appendChild(btnEdit);

            const btnDelete = document.createElement('button');
            btnDelete.className = 'btn-action-sm btn-reject';
            btnDelete.textContent = 'Xóa';
            btnDelete.addEventListener('click', () => handleDeleteSla(s.id, s.priority));
            group.appendChild(btnDelete);

            tdActions.appendChild(group);
            tr.appendChild(tdActions);

            slaTableBody.appendChild(tr);
        });
    };

    const loadSlaConfigs = async () => {
        slaLoading.hidden = false;
        slaEmpty.hidden = true;
        slaContent.hidden = true;

        try {
            const res = await fetch('/api/sla');
            const payload = await res.json();
            if (!res.ok || !payload.success) throw new Error(payload.message || 'Lỗi tải cấu hình SLA.');

            allSlaConfigs = payload.data || [];
            renderTable();
        } catch (err) {
            showAlert(err.message, 'error');
            slaEmpty.hidden = false;
        } finally {
            slaLoading.hidden = true;
        }
    };

    const openCreateModal = () => {
        slaIdInput.value = '';
        slaModalTitle.textContent = 'Thêm cấu hình SLA';
        inputSlaPriority.value = 'MEDIUM';
        inputResponseTime.value = 4;
        inputResolutionTime.value = 24;
        inputWarningTime.value = 2;
        inputSlaStatus.value = 'ACTIVE';
        slaModal.hidden = false;
        inputResponseTime.focus();
    };

    const openEditModal = (s) => {
        slaIdInput.value = s.id;
        slaModalTitle.textContent = `Chỉnh sửa SLA: ${s.priority}`;
        inputSlaPriority.value = s.priority;
        inputResponseTime.value = s.responseTimeHours || s.responseTime || '';
        inputResolutionTime.value = s.resolutionTimeHours || s.resolutionTime || '';
        inputWarningTime.value = s.warningTimeHours || s.warningTime || '';
        inputSlaStatus.value = s.status || 'ACTIVE';
        slaModal.hidden = false;
        inputResponseTime.focus();
    };

    if (btnCloseModal) {
        btnCloseModal.addEventListener('click', () => { slaModal.hidden = true; });
    }

    if (btnOpenCreate) {
        btnOpenCreate.addEventListener('click', openCreateModal);
    }

    slaForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = slaIdInput.value;
        const isEdit = Boolean(id);

        const resp = parseInt(inputResponseTime.value, 10);
        const resol = parseInt(inputResolutionTime.value, 10);
        const warn = parseInt(inputWarningTime.value, 10);

        if (warn > resp || resp > resol) {
            alert('Quy tắc thời gian không hợp lệ: Thời gian cảnh báo ≤ Thời gian phản hồi ≤ Thời gian hoàn thành giải quyết.');
            return;
        }

        const payload = {
            priority: inputSlaPriority.value,
            responseTime: resp,
            responseTimeHours: resp,
            resolutionTime: resol,
            resolutionTimeHours: resol,
            warningTime: warn,
            warningTimeHours: warn,
            status: inputSlaStatus.value
        };

        const submitBtn = document.getElementById('btnSlaModalSubmit');
        if (submitBtn) submitBtn.disabled = true;

        try {
            const url = isEdit ? `/api/sla/${id}` : '/api/sla';
            const method = isEdit ? 'PUT' : 'POST';

            const res = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                body: JSON.stringify(payload)
            });

            const data = await res.json();
            if (!res.ok || !data.success) throw new Error(data.message || 'Lưu cấu hình SLA thất bại.');

            slaModal.hidden = true;
            showAlert(isEdit ? 'Cập nhật SLA thành công!' : 'Tạo cấu hình SLA mới thành công!');
            loadSlaConfigs();
        } catch (err) {
            alert('Lỗi: ' + err.message);
        } finally {
            if (submitBtn) submitBtn.disabled = false;
        }
    });

    const handleDeleteSla = async (id, priority) => {
        if (!confirm(`Bạn có chắc chắn muốn xóa cấu hình SLA cho mức độ "${priority}"?`)) return;

        try {
            const res = await fetch(`/api/sla/${encodeURIComponent(id)}`, { method: 'DELETE' });
            if (!res.ok) throw new Error('Xóa SLA thất bại.');

            showAlert(`Đã xóa cấu hình SLA cho "${priority}" thành công!`);
            loadSlaConfigs();
        } catch (err) {
            showAlert(err.message, 'error');
        }
    };

    loadSlaConfigs();
});
