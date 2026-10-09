// JavaScript cho trang Login
document.addEventListener('DOMContentLoaded', () => {
    const passwordInput = document.getElementById('password');
    const toggleBtn = document.getElementById('togglePasswordBtn');

    if (toggleBtn && passwordInput) {
        toggleBtn.addEventListener('click', () => {
            const isPassword = passwordInput.type === 'password';
            passwordInput.type = isPassword ? 'text' : 'password';
            toggleBtn.textContent = isPassword ? '🙈' : '👁';
        });
    }
});

// Tiện ích click để tự động điền tài khoản mẫu
window.fillLogin = (username, password) => {
    const uInput = document.getElementById('username');
    const pInput = document.getElementById('password');
    if (uInput && pInput) {
        uInput.value = username;
        pInput.value = password;
        uInput.focus();
    }
};
