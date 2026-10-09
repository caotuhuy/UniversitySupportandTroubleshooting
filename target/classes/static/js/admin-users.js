// JavaScript cho trang Quản lý Người dùng của Admin
document.addEventListener('DOMContentLoaded', () => {
    let allUsers = [];

    const userLoading = document.getElementById('userLoading');
    const userEmpty = document.getElementById('userEmpty');
    const userContent = document.getElementById('userContent');
    const userTableBody = document.getElementById('userTableBody');
    const userAlert = document.getElementById('userAlert');

    const userSearchInput = document.getElementById('userSearchInput');
    const userRoleFilter = document.getElementById('userRoleFilter');
    const userStatusFilter = document.getElementById('userStatusFilter');

    const userModal = document.getElementById('userModal');
    const userModalTitle = document.getElementById('userModalTitle');
    const userForm = document.getElementById('userForm');
    const userIdInput = document.getElementById('userIdInput');
    const inputUsername = document.getElementById('inputUsername');
    const inputPassword = document.getElementById('inputPassword');
    const pwdRequiredSpan = document.getElementById('pwdRequiredSpan');
    const inputFullName = document.getElementById('inputFullName');
    const inputEmail = document.getElementById('inputEmail');
    const inputPhone = document.getElementById('inputPhone');
    const inputStudentCode = document.getElementById('inputStudentCode');
    const roleCheckboxes = document.querySelectorAll('input[name="roleCheckbox"]');

    const btnOpenCreate = document.getElementById('btnOpenCreateUserModal');
    const btnCloseModal = document.getElementById('btnUserModalClose');

    const showAlert = (msg, type = 'success') => {
        if (!userAlert) return;
        userAlert.className = `tech-alert ${type}`;
        userAlert.textContent = msg;
        userAlert.style.display = 'block';
        window.scrollTo({ top: 0, behavior: 'smooth' });
        setTimeout(() => { userAlert.style.display = 'none'; }, 4000);
    };

    const renderTable = () => {
        userTableBody.innerHTML = '';
        const keyword = (userSearchInput?.value || '').toLowerCase().trim();
        const roleVal = userRoleFilter?.value || 'ALL';
        const statusVal = userStatusFilter?.value || 'ALL';

        const filtered = allUsers.filter(u => {
            if (roleVal !== 'ALL') {
                const hasRole = u.roles?.some(r => r.name === roleVal);
                if (!hasRole) return false;
            }
            if (statusVal !== 'ALL') {
                if (u.status !== statusVal) return false;
            }
            if (keyword) {
                const matchU = (u.username || '').toLowerCase().includes(keyword);
                const matchN = (u.fullName || '').toLowerCase().includes(keyword);
                const matchE = (u.email || '').toLowerCase().includes(keyword);
                return matchU || matchN || matchE;
            }
            return true;
        });

        if (filtered.length === 0) {
            userEmpty.hidden = false;
            userContent.hidden = true;
            return;
        }

        userEmpty.hidden = true;
        userContent.hidden = false;

        filtered.forEach(u => {
            const tr = document.createElement('tr');

            // ID
            const tdId = document.createElement('td');
            tdId.textContent = u.id;
            tr.appendChild(tdId);

            // Username
            const tdUsername = document.createElement('td');
            const uBadge = document.createElement('code');
            uBadge.textContent = u.username;
            tdUsername.appendChild(uBadge);
            tr.appendChild(tdUsername);

            // Full Name
            const tdName = document.createElement('td');
            tdName.textContent = u.fullName || '—';
            tdName.style.fontWeight = '600';
            tr.appendChild(tdName);

            // Email
            const tdEmail = document.createElement('td');
            tdEmail.textContent = u.email || '—';
            tr.appendChild(tdEmail);

            // Phone
            const tdPhone = document.createElement('td');
            tdPhone.textContent = u.phone || '—';
            tr.appendChild(tdPhone);

            // Code
            const tdCode = document.createElement('td');
            tdCode.textContent = u.studentCode || u.employeeCode || '—';
            tr.appendChild(tdCode);

            // Roles
            const tdRoles = document.createElement('td');
            if (u.roles && u.roles.length > 0) {
                u.roles.forEach(r => {
                    const badge = document.createElement('span');
                    badge.className = 'role-badge';
                    badge.textContent = r.name.replace('ROLE_', '');
                    badge.style.marginRight = '4px';
                    tdRoles.appendChild(badge);
                });
            } else {
                tdRoles.textContent = '—';
            }
            tr.appendChild(tdRoles);

            // Status
            const tdStatus = document.createElement('td');
            const sBadge = document.createElement('span');
            const isLocked = u.status === 'LOCKED';
            sBadge.className = isLocked ? 'request-status status-rejected' : 'request-status status-completed';
            sBadge.textContent = isLocked ? 'Đã khóa' : 'Hoạt động';
            tdStatus.appendChild(sBadge);
            tr.appendChild(tdStatus);

            // Actions
            const tdActions = document.createElement('td');
            const group = document.createElement('div');
            group.className = 'btn-action-group';

            // Edit
            const btnEdit = document.createElement('button');
            btnEdit.className = 'btn-action-sm btn-view';
            btnEdit.textContent = 'Sửa';
            btnEdit.addEventListener('click', () => openEditModal(u));
            group.appendChild(btnEdit);

            // Lock / Unlock
            const btnLock = document.createElement('button');
            btnLock.className = isLocked ? 'btn-action-sm btn-accept' : 'btn-action-sm btn-reject';
            btnLock.textContent = isLocked ? 'Mở khóa' : 'Khóa';
            btnLock.addEventListener('click', () => handleToggleLock(u.id, isLocked));
            group.appendChild(btnLock);

            // Delete
            const btnDelete = document.createElement('button');
            btnDelete.className = 'btn-action-sm btn-reject';
            btnDelete.textContent = 'Xóa';
            btnDelete.addEventListener('click', () => handleDeleteUser(u.id, u.username));
            group.appendChild(btnDelete);

            tdActions.appendChild(group);
            tr.appendChild(tdActions);

            userTableBody.appendChild(tr);
        });
    };

    const loadUsers = async () => {
        userLoading.hidden = false;
        userEmpty.hidden = true;
        userContent.hidden = true;

        try {
            const res = await fetch('/api/users');
            const payload = await res.json();
            if (!res.ok || !payload.success) throw new Error(payload.message || 'Lỗi tải danh sách người dùng.');

            allUsers = payload.data || [];
            renderTable();
        } catch (err) {
            showAlert(err.message, 'error');
            userEmpty.hidden = false;
        } finally {
            userLoading.hidden = true;
        }
    };

    const openCreateModal = () => {
        userIdInput.value = '';
        userModalTitle.textContent = 'Thêm người dùng mới';
        inputUsername.disabled = false;
        inputUsername.value = '';
        inputPassword.value = '';
        inputPassword.required = true;
        pwdRequiredSpan.hidden = false;
        inputFullName.value = '';
        inputEmail.value = '';
        inputPhone.value = '';
        inputStudentCode.value = '';
        roleCheckboxes.forEach(cb => { cb.checked = cb.value === '1'; }); // Default student
        userModal.hidden = false;
        inputUsername.focus();
    };

    const openEditModal = (u) => {
        userIdInput.value = u.id;
        userModalTitle.textContent = `Chỉnh sửa: ${u.username}`;
        inputUsername.disabled = true;
        inputUsername.value = u.username;
        inputPassword.value = '';
        inputPassword.required = false;
        pwdRequiredSpan.hidden = true;
        inputFullName.value = u.fullName || '';
        inputEmail.value = u.email || '';
        inputPhone.value = u.phone || '';
        inputStudentCode.value = u.studentCode || u.employeeCode || '';

        const roleNames = u.roles?.map(r => r.name) || [];
        roleCheckboxes.forEach(cb => {
            if (cb.value === '1') cb.checked = roleNames.includes('ROLE_STUDENT');
            if (cb.value === '2') cb.checked = roleNames.includes('ROLE_TECHNICIAN');
            if (cb.value === '3') cb.checked = roleNames.includes('ROLE_ADMIN');
        });

        userModal.hidden = false;
        inputFullName.focus();
    };

    if (btnCloseModal) {
        btnCloseModal.addEventListener('click', () => { userModal.hidden = true; });
    }

    if (btnOpenCreate) {
        btnOpenCreate.addEventListener('click', openCreateModal);
    }

    userForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = userIdInput.value;
        const isEdit = Boolean(id);

        const selectedRoleIds = Array.from(roleCheckboxes)
                .filter(cb => cb.checked)
                .map(cb => parseInt(cb.value, 10));

        if (selectedRoleIds.length === 0) {
            alert('Vui lòng chọn ít nhất một vai trò cho người dùng.');
            return;
        }

        const payload = {
            username: inputUsername.value.trim(),
            password: inputPassword.value.trim(),
            fullName: inputFullName.value.trim(),
            email: inputEmail.value.trim(),
            phone: inputPhone.value.trim(),
            studentCode: inputStudentCode.value.trim(),
            employeeCode: inputStudentCode.value.trim(),
            roleIds: selectedRoleIds
        };

        const submitBtn = document.getElementById('btnUserModalSubmit');
        if (submitBtn) submitBtn.disabled = true;

        try {
            const url = isEdit ? `/api/users/${id}` : '/api/users';
            const method = isEdit ? 'PUT' : 'POST';

            const res = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                body: JSON.stringify(payload)
            });

            const data = await res.json();
            if (!res.ok || !data.success) {
                throw new Error(data.message || 'Lưu thông tin người dùng thất bại.');
            }

            userModal.hidden = true;
            showAlert(isEdit ? 'Cập nhật người dùng thành công!' : 'Tạo người dùng mới thành công!');
            loadUsers();
        } catch (err) {
            alert('Lỗi: ' + err.message);
        } finally {
            if (submitBtn) submitBtn.disabled = false;
        }
    });

    const handleToggleLock = async (id, isLocked) => {
        const action = isLocked ? 'unlock' : 'lock';
        const label = isLocked ? 'mở khóa' : 'khóa';
        if (!confirm(`Bạn có chắc chắn muốn ${label} tài khoản này?`)) return;

        try {
            const res = await fetch(`/api/users/${encodeURIComponent(id)}/${action}`, {
                method: 'PUT',
                headers: { Accept: 'application/json' }
            });
            const data = await res.json();
            if (!res.ok || !data.success) throw new Error(data.message || `${label} thất bại.`);

            showAlert(`Đã ${label} tài khoản thành công!`);
            loadUsers();
        } catch (err) {
            showAlert(err.message, 'error');
        }
    };

    const handleDeleteUser = async (id, username) => {
        if (!confirm(`Bạn có chắc chắn muốn xóa tài khoản "${username}"? Thao tác này không thể hoàn tác!`)) return;

        try {
            const res = await fetch(`/api/users/${encodeURIComponent(id)}`, {
                method: 'DELETE'
            });
            if (!res.ok) throw new Error('Xóa người dùng thất bại.');

            showAlert(`Đã xóa người dùng "${username}" thành công!`);
            loadUsers();
        } catch (err) {
            showAlert(err.message, 'error');
        }
    };

    if (userSearchInput) userSearchInput.addEventListener('input', renderTable);
    if (userRoleFilter) userRoleFilter.addEventListener('change', renderTable);
    if (userStatusFilter) userStatusFilter.addEventListener('change', renderTable);

    loadUsers();
});
