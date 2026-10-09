// JavaScript cho trang Quản lý Thiết bị của Admin
document.addEventListener('DOMContentLoaded', () => {
    let allDevices = [];
    let allRooms = [];

    const deviceLoading = document.getElementById('deviceLoading');
    const deviceEmpty = document.getElementById('deviceEmpty');
    const deviceContent = document.getElementById('deviceContent');
    const deviceTableBody = document.getElementById('deviceTableBody');
    const deviceAlert = document.getElementById('deviceAlert');
    const deviceSearchInput = document.getElementById('deviceSearchInput');
    const deviceStatusFilter = document.getElementById('deviceStatusFilter');

    const deviceModal = document.getElementById('deviceModal');
    const deviceModalTitle = document.getElementById('deviceModalTitle');
    const deviceForm = document.getElementById('deviceForm');
    const deviceIdInput = document.getElementById('deviceIdInput');
    const inputDeviceCode = document.getElementById('inputDeviceCode');
    const inputDeviceName = document.getElementById('inputDeviceName');
    const inputDeviceType = document.getElementById('inputDeviceType');
    const inputDeviceRoom = document.getElementById('inputDeviceRoom');
    const inputDeviceStatus = document.getElementById('inputDeviceStatus');

    const btnOpenCreate = document.getElementById('btnOpenCreateDevice');
    const btnCloseModal = document.getElementById('btnDeviceModalClose');

    const showAlert = (msg, type = 'success') => {
        if (!deviceAlert) return;
        deviceAlert.className = `tech-alert ${type}`;
        deviceAlert.textContent = msg;
        deviceAlert.style.display = 'block';
        window.scrollTo({ top: 0, behavior: 'smooth' });
        setTimeout(() => { deviceAlert.style.display = 'none'; }, 4000);
    };

    const renderTable = () => {
        deviceTableBody.innerHTML = '';
        const keyword = (deviceSearchInput?.value || '').toLowerCase().trim();
        const statusVal = deviceStatusFilter?.value || 'ALL';

        const filtered = allDevices.filter(d => {
            if (statusVal !== 'ALL' && d.status !== statusVal) return false;
            if (keyword) {
                const matchC = (d.deviceCode || '').toLowerCase().includes(keyword);
                const matchN = (d.deviceName || '').toLowerCase().includes(keyword);
                const matchT = (d.deviceType || '').toLowerCase().includes(keyword);
                return matchC || matchN || matchT;
            }
            return true;
        });

        if (filtered.length === 0) {
            deviceEmpty.hidden = false;
            deviceContent.hidden = true;
            return;
        }

        deviceEmpty.hidden = true;
        deviceContent.hidden = false;

        filtered.forEach(d => {
            const tr = document.createElement('tr');

            // ID
            const tdId = document.createElement('td');
            tdId.textContent = d.id;
            tr.appendChild(tdId);

            // Code
            const tdCode = document.createElement('td');
            const cBadge = document.createElement('code');
            cBadge.textContent = d.deviceCode;
            tdCode.appendChild(cBadge);
            tr.appendChild(tdCode);

            // Name
            const tdName = document.createElement('td');
            tdName.textContent = d.deviceName || '—';
            tdName.style.fontWeight = '600';
            tr.appendChild(tdName);

            // Type
            const tdType = document.createElement('td');
            tdType.textContent = d.deviceType || '—';
            tr.appendChild(tdType);

            // Room
            const tdRoom = document.createElement('td');
            tdRoom.textContent = d.room ? (d.room.roomCode || d.room.roomName) : '—';
            tr.appendChild(tdRoom);

            // Status
            const tdStatus = document.createElement('td');
            const sBadge = document.createElement('span');
            let sClass = 'status-completed';
            let sText = 'Hoạt động';
            if (d.status === 'MAINTENANCE') { sClass = 'status-in_progress'; sText = 'Bảo trì'; }
            else if (d.status === 'BROKEN') { sClass = 'status-rejected'; sText = 'Hỏng hóc'; }
            else if (d.status === 'INACTIVE') { sClass = 'status-cancelled'; sText = 'Ngừng dùng'; }
            sBadge.className = `request-status ${sClass}`;
            sBadge.textContent = sText;
            tdStatus.appendChild(sBadge);
            tr.appendChild(tdStatus);

            // Actions
            const tdActions = document.createElement('td');
            const group = document.createElement('div');
            group.className = 'btn-action-group';

            const btnEdit = document.createElement('button');
            btnEdit.className = 'btn-action-sm btn-view';
            btnEdit.textContent = 'Sửa';
            btnEdit.addEventListener('click', () => openEditModal(d));
            group.appendChild(btnEdit);

            const btnDelete = document.createElement('button');
            btnDelete.className = 'btn-action-sm btn-reject';
            btnDelete.textContent = 'Xóa';
            btnDelete.addEventListener('click', () => handleDeleteDevice(d.id, d.deviceName));
            group.appendChild(btnDelete);

            tdActions.appendChild(group);
            tr.appendChild(tdActions);

            deviceTableBody.appendChild(tr);
        });
    };

    const loadData = async () => {
        deviceLoading.hidden = false;
        deviceEmpty.hidden = true;
        deviceContent.hidden = true;

        try {
            // Load rooms
            const rRes = await fetch('/api/rooms');
            if (rRes.ok) {
                const rData = await rRes.json();
                if (rData.success) {
                    allRooms = rData.data || [];
                    populateRoomDropdown();
                }
            }

            // Load devices
            const dRes = await fetch('/api/devices');
            const dData = await dRes.json();
            if (!dRes.ok || !dData.success) throw new Error(dData.message || 'Lỗi tải danh sách thiết bị.');

            allDevices = dData.data || [];
            renderTable();
        } catch (err) {
            showAlert(err.message, 'error');
            deviceEmpty.hidden = false;
        } finally {
            deviceLoading.hidden = true;
        }
    };

    const populateRoomDropdown = () => {
        inputDeviceRoom.innerHTML = '<option value="">-- Không gán phòng --</option>';
        allRooms.forEach(r => {
            const opt = document.createElement('option');
            opt.value = r.id;
            opt.textContent = `${r.roomCode} - ${r.roomName}`;
            inputDeviceRoom.appendChild(opt);
        });
    };

    const openCreateModal = () => {
        deviceIdInput.value = '';
        deviceModalTitle.textContent = 'Thêm thiết bị mới';
        inputDeviceCode.value = '';
        inputDeviceName.value = '';
        inputDeviceType.value = '';
        inputDeviceRoom.value = '';
        inputDeviceStatus.value = 'ACTIVE';
        deviceModal.hidden = false;
        inputDeviceCode.focus();
    };

    const openEditModal = (d) => {
        deviceIdInput.value = d.id;
        deviceModalTitle.textContent = `Chỉnh sửa: ${d.deviceName}`;
        inputDeviceCode.value = d.deviceCode || '';
        inputDeviceName.value = d.deviceName || '';
        inputDeviceType.value = d.deviceType || '';
        inputDeviceRoom.value = d.room ? d.room.id : '';
        inputDeviceStatus.value = d.status || 'ACTIVE';
        deviceModal.hidden = false;
        inputDeviceName.focus();
    };

    if (btnCloseModal) {
        btnCloseModal.addEventListener('click', () => { deviceModal.hidden = true; });
    }

    if (btnOpenCreate) {
        btnOpenCreate.addEventListener('click', openCreateModal);
    }

    deviceForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = deviceIdInput.value;
        const isEdit = Boolean(id);

        const payload = {
            deviceCode: inputDeviceCode.value.trim(),
            deviceName: inputDeviceName.value.trim(),
            deviceType: inputDeviceType.value.trim(),
            roomId: inputDeviceRoom.value ? parseInt(inputDeviceRoom.value, 10) : null,
            status: inputDeviceStatus.value
        };

        const submitBtn = document.getElementById('btnDeviceModalSubmit');
        if (submitBtn) submitBtn.disabled = true;

        try {
            const url = isEdit ? `/api/devices/${id}` : '/api/devices';
            const method = isEdit ? 'PUT' : 'POST';

            const res = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                body: JSON.stringify(payload)
            });

            const data = await res.json();
            if (!res.ok || !data.success) throw new Error(data.message || 'Lưu thông tin thiết bị thất bại.');

            deviceModal.hidden = true;
            showAlert(isEdit ? 'Cập nhật thiết bị thành công!' : 'Tạo thiết bị mới thành công!');
            loadData();
        } catch (err) {
            alert('Lỗi: ' + err.message);
        } finally {
            if (submitBtn) submitBtn.disabled = false;
        }
    });

    const handleDeleteDevice = async (id, name) => {
        if (!confirm(`Bạn có chắc chắn muốn xóa thiết bị "${name}"?`)) return;

        try {
            const res = await fetch(`/api/devices/${encodeURIComponent(id)}`, { method: 'DELETE' });
            if (!res.ok) throw new Error('Xóa thiết bị thất bại.');

            showAlert(`Đã xóa thiết bị "${name}" thành công!`);
            loadData();
        } catch (err) {
            showAlert(err.message, 'error');
        }
    };

    if (deviceSearchInput) deviceSearchInput.addEventListener('input', renderTable);
    if (deviceStatusFilter) deviceStatusFilter.addEventListener('change', renderTable);

    loadData();
});
