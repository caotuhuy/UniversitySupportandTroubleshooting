// JavaScript cho trang Hồ sơ cá nhân
document.addEventListener('DOMContentLoaded', () => {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    const alertBox = document.getElementById('profileAlert');

    const profileForm = document.getElementById('profileForm');
    const changePasswordForm = document.getElementById('changePasswordForm');

    // Tab switching
    const switchTab = (tabId) => {
        tabButtons.forEach(btn => {
            btn.classList.toggle('active', btn.dataset.tab === tabId);
        });
        tabContents.forEach(content => {
            content.classList.toggle('active', content.id === tabId);
        });
    };

    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            switchTab(btn.dataset.tab);
        });
    });

    // Hash or URL param handling
    if (window.location.hash === '#password' || window.location.search.includes('tab=password')) {
        switchTab('passwordTab');
    }

    const showAlert = (message, type = 'success') => {
        if (!alertBox) return;
        alertBox.className = `profile-alert ${type}`;
        alertBox.textContent = message;
        alertBox.style.display = 'block';
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const hideAlert = () => {
        if (alertBox) alertBox.style.display = 'none';
    };

    // Tải thông tin tài khoản hiện tại
    const loadProfile = async () => {
        try {
            const res = await fetch('/api/auth/me', {
                headers: { 'Accept': 'application/json' }
            });
            if (res.status === 401) {
                window.location.href = '/login';
                return;
            }
            const data = await res.json();
            if (!res.ok || !data.success || !data.data) {
                throw new Error(data.message || 'Không thể tải thông tin cá nhân');
            }

            const user = data.data;
            const initials = (user.fullName || user.username || 'U').substring(0, 2).toUpperCase();
            
            const avatarEl = document.getElementById('profileAvatar');
            const nameEl = document.getElementById('profileDisplayFullName');
            const roleEl = document.getElementById('profileDisplayRole');

            if (avatarEl) avatarEl.textContent = initials;
            if (nameEl) nameEl.textContent = user.fullName || user.username;
            
            const rolesText = user.roles ? user.roles.map(r => r.name.replace('ROLE_', '')).join(', ') : 'Người dùng';
            if (roleEl) roleEl.textContent = rolesText;

            document.getElementById('profileUsername').value = user.username || '';
            document.getElementById('profileCode').value = user.studentCode || user.employeeCode || '—';
            document.getElementById('profileFullName').value = user.fullName || '';
            document.getElementById('profileEmail').value = user.email || '';
            document.getElementById('profilePhone').value = user.phone || '';
            document.getElementById('profileStatus').value = user.status === 'ACTIVE' ? 'Đang hoạt động' : (user.status || 'Hoạt động');

        } catch (error) {
            showAlert(error.message, 'error');
        }
    };

    // Xử lý cập nhật thông tin cá nhân
    profileForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert();

        const btn = document.getElementById('btnSaveProfile');
        if (btn) btn.disabled = true;

        const payload = {
            fullName: document.getElementById('profileFullName').value.trim(),
            email: document.getElementById('profileEmail').value.trim(),
            phone: document.getElementById('profilePhone').value.trim()
        };

        try {
            const res = await fetch('/api/auth/profile', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            const data = await res.json();
            if (!res.ok || !data.success) {
                throw new Error(data.message || 'Cập nhật thông tin thất bại');
            }

            showAlert('Cập nhật thông tin cá nhân thành công!', 'success');
            loadProfile();
        } catch (error) {
            showAlert(error.message, 'error');
        } finally {
            if (btn) btn.disabled = false;
        }
    });

    // Xử lý đổi mật khẩu
    changePasswordForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert();

        const currentPassword = document.getElementById('currentPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        if (newPassword.length < 6) {
            showAlert('Mật khẩu mới phải có tối thiểu 6 ký tự.', 'error');
            return;
        }

        if (newPassword !== confirmPassword) {
            showAlert('Xác nhận mật khẩu mới không khớp.', 'error');
            return;
        }

        const btn = document.getElementById('btnChangePassword');
        if (btn) btn.disabled = true;

        try {
            const res = await fetch('/api/auth/change-password', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({ currentPassword, newPassword, confirmPassword })
            });

            const data = await res.json();
            if (!res.ok || !data.success) {
                throw new Error(data.message || 'Đổi mật khẩu thất bại');
            }

            showAlert('Đổi mật khẩu thành công! Vui lòng sử dụng mật khẩu mới cho lần đăng nhập tiếp theo.', 'success');
            changePasswordForm.reset();
        } catch (error) {
            showAlert(error.message, 'error');
        } finally {
            if (btn) btn.disabled = false;
        }
    });

    loadProfile();
});
