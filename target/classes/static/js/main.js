document.addEventListener('DOMContentLoaded', () => {
    const btnTest = document.getElementById('btnTestApi');
    const apiResult = document.getElementById('apiResult');

    if (btnTest && apiResult) {
        btnTest.addEventListener('click', async () => {
            btnTest.innerText = 'Đang gọi API...';
            try {
                const response = await fetch('/api/test');
                const data = await response.json();
                apiResult.textContent = JSON.stringify(data, null, 4);
            } catch (error) {
                apiResult.textContent = JSON.stringify({ success: false, message: error.message }, null, 4);
            } finally {
                btnTest.innerText = 'Gọi GET /api/test';
            }
        });
    }
});
