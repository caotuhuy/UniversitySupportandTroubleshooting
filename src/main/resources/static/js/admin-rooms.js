// JavaScript cho trang Quản lý Phòng của Admin
document.addEventListener('DOMContentLoaded', () => {
    let allRooms = [];

    const roomLoading = document.getElementById('roomLoading');
    const roomEmpty = document.getElementById('roomEmpty');
    const roomContent = document.getElementById('roomContent');
    const roomTableBody = document.getElementById('roomTableBody');
    const roomAlert = document.getElementById('roomAlert');
    const roomSearchInput = document.getElementById('roomSearchInput');

    const roomModal = document.getElementById('roomModal');
    const roomModalTitle = document.getElementById('roomModalTitle');
    const roomForm = document.getElementById('roomForm');
    const roomIdInput = document.getElementById('roomIdInput');
    const inputRoomCode = document.getElementById('inputRoomCode');
    const inputRoomName = document.getElementById('inputRoomName');
    const inputBuilding = document.getElementById('inputBuilding');
    const inputFloor = document.getElementById('inputFloor');
    const inputRoomStatus = document.getElementById('inputRoomStatus');

    const btnOpenCreate = document.getElementById('btnOpenCreateRoom');
    const btnCloseModal = document.getElementById('btnRoomModalClose');

    const showAlert = (msg, type = 'success') => {
        if (!roomAlert) return;
        roomAlert.className = `tech-alert ${type}`;
        roomAlert.textContent = msg;
        roomAlert.style.display = 'block';
        window.scrollTo({ top: 0, behavior: 'smooth' });
        setTimeout(() => { roomAlert.style.display = 'none'; }, 4000);
    };

    const renderTable = () => {
        roomTableBody.innerHTML = '';
        const keyword = (roomSearchInput?.value || '').toLowerCase().trim();

        const filtered = allRooms.filter(r => {
            if (keyword) {
                const matchC = (r.roomCode || '').toLowerCase().includes(keyword);
                const matchN = (r.roomName || '').toLowerCase().includes(keyword);
                const matchB = (r.building || '').toLowerCase().includes(keyword);
                return matchC || matchN || matchB;
            }
            return true;
        });

        if (filtered.length === 0) {
            roomEmpty.hidden = false;
            roomContent.hidden = true;
            return;
        }

        roomEmpty.hidden = true;
        roomContent.hidden = false;

        filtered.forEach(r => {
            const tr = document.createElement('tr');

            // ID
            const tdId = document.createElement('td');
            tdId.textContent = r.id;
            tr.appendChild(tdId);

            // Code
            const tdCode = document.createElement('td');
            const cBadge = document.createElement('code');
            cBadge.textContent = r.roomCode;
            tdCode.appendChild(cBadge);
            tr.appendChild(tdCode);

            // Name
            const tdName = document.createElement('td');
            tdName.textContent = r.roomName || '—';
            tdName.style.fontWeight = '600';
            tr.appendChild(tdName);

            // Building
            const tdB = document.createElement('td');
            tdB.textContent = r.building || '—';
            tr.appendChild(tdB);

            // Floor
            const tdF = document.createElement('td');
            tdF.textContent = r.floor || '—';
            tr.appendChild(tdF);

            // Status
            const tdStatus = document.createElement('td');
            const sBadge = document.createElement('span');
            const isActive = r.status === 'ACTIVE';
            sBadge.className = isActive ? 'request-status status-completed' : 'request-status status-cancelled';
            sBadge.textContent = isActive ? 'Hoạt động' : (r.status === 'MAINTENANCE' ? 'Bảo trì' : 'Ngừng dùng');
            tdStatus.appendChild(sBadge);
            tr.appendChild(tdStatus);

            // Actions
            const tdActions = document.createElement('td');
            const group = document.createElement('div');
            group.className = 'btn-action-group';

            const btnEdit = document.createElement('button');
            btnEdit.className = 'btn-action-sm btn-view';
            btnEdit.textContent = 'Sửa';
            btnEdit.addEventListener('click', () => openEditModal(r));
            group.appendChild(btnEdit);

            const btnDelete = document.createElement('button');
            btnDelete.className = 'btn-action-sm btn-reject';
            btnDelete.textContent = 'Xóa';
            btnDelete.addEventListener('click', () => handleDeleteRoom(r.id, r.roomName));
            group.appendChild(btnDelete);

            tdActions.appendChild(group);
            tr.appendChild(tdActions);

            roomTableBody.appendChild(tr);
        });
    };

    const loadRooms = async () => {
        roomLoading.hidden = false;
        roomEmpty.hidden = true;
        roomContent.hidden = true;

        try {
            const res = await fetch('/api/rooms');
            const payload = await res.json();
            if (!res.ok || !payload.success) throw new Error(payload.message || 'Lỗi tải danh sách phòng.');

            allRooms = payload.data || [];
            renderTable();
        } catch (err) {
            showAlert(err.message, 'error');
            roomEmpty.hidden = false;
        } finally {
            roomLoading.hidden = true;
        }
    };

    const openCreateModal = () => {
        roomIdInput.value = '';
        roomModalTitle.textContent = 'Thêm phòng mới';
        inputRoomCode.value = '';
        inputRoomName.value = '';
        inputBuilding.value = '';
        inputFloor.value = '';
        inputRoomStatus.value = 'ACTIVE';
        roomModal.hidden = false;
        inputRoomCode.focus();
    };

    const openEditModal = (r) => {
        roomIdInput.value = r.id;
        roomModalTitle.textContent = `Chỉnh sửa: ${r.roomName}`;
        inputRoomCode.value = r.roomCode || '';
        inputRoomName.value = r.roomName || '';
        inputBuilding.value = r.building || '';
        inputFloor.value = r.floor || '';
        inputRoomStatus.value = r.status || 'ACTIVE';
        roomModal.hidden = false;
        inputRoomName.focus();
    };

    if (btnCloseModal) {
        btnCloseModal.addEventListener('click', () => { roomModal.hidden = true; });
    }

    if (btnOpenCreate) {
        btnOpenCreate.addEventListener('click', openCreateModal);
    }

    roomForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = roomIdInput.value;
        const isEdit = Boolean(id);

        const payload = {
            roomCode: inputRoomCode.value.trim(),
            roomName: inputRoomName.value.trim(),
            building: inputBuilding.value.trim(),
            floor: inputFloor.value.trim(),
            status: inputRoomStatus.value
        };

        const submitBtn = document.getElementById('btnRoomModalSubmit');
        if (submitBtn) submitBtn.disabled = true;

        try {
            const url = isEdit ? `/api/rooms/${id}` : '/api/rooms';
            const method = isEdit ? 'PUT' : 'POST';

            const res = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                body: JSON.stringify(payload)
            });

            const data = await res.json();
            if (!res.ok || !data.success) throw new Error(data.message || 'Lưu thông tin phòng thất bại.');

            roomModal.hidden = true;
            showAlert(isEdit ? 'Cập nhật phòng thành công!' : 'Tạo phòng mới thành công!');
            loadRooms();
        } catch (err) {
            alert('Lỗi: ' + err.message);
        } finally {
            if (submitBtn) submitBtn.disabled = false;
        }
    });

    const handleDeleteRoom = async (id, name) => {
        if (!confirm(`Bạn có chắc chắn muốn xóa phòng "${name}"?`)) return;

        try {
            const res = await fetch(`/api/rooms/${encodeURIComponent(id)}`, { method: 'DELETE' });
            if (!res.ok) throw new Error('Xóa phòng thất bại.');

            showAlert(`Đã xóa phòng "${name}" thành công!`);
            loadRooms();
        } catch (err) {
            showAlert(err.message, 'error');
        }
    };

    if (roomSearchInput) roomSearchInput.addEventListener('input', renderTable);

    loadRooms();
});
