// JavaScript cho trang Quản lý Danh mục của Admin
document.addEventListener('DOMContentLoaded', () => {
    let allCategories = [];

    const catLoading = document.getElementById('catLoading');
    const catEmpty = document.getElementById('catEmpty');
    const catContent = document.getElementById('catContent');
    const catTableBody = document.getElementById('catTableBody');
    const catAlert = document.getElementById('catAlert');
    const catSearchInput = document.getElementById('catSearchInput');

    const catModal = document.getElementById('catModal');
    const catModalTitle = document.getElementById('catModalTitle');
    const catForm = document.getElementById('catForm');
    const catIdInput = document.getElementById('catIdInput');
    const inputCatName = document.getElementById('inputCatName');
    const inputCatDesc = document.getElementById('inputCatDesc');
    const inputCatStatus = document.getElementById('inputCatStatus');

    const btnOpenCreate = document.getElementById('btnOpenCreateCat');
    const btnCloseModal = document.getElementById('btnCatModalClose');

    const showAlert = (msg, type = 'success') => {
        if (!catAlert) return;
        catAlert.className = `tech-alert ${type}`;
        catAlert.textContent = msg;
        catAlert.style.display = 'block';
        window.scrollTo({ top: 0, behavior: 'smooth' });
        setTimeout(() => { catAlert.style.display = 'none'; }, 4000);
    };

    const renderTable = () => {
        catTableBody.innerHTML = '';
        const keyword = (catSearchInput?.value || '').toLowerCase().trim();

        const filtered = allCategories.filter(c => {
            if (keyword) {
                const matchName = (c.name || '').toLowerCase().includes(keyword);
                const matchDesc = (c.description || '').toLowerCase().includes(keyword);
                return matchName || matchDesc;
            }
            return true;
        });

        if (filtered.length === 0) {
            catEmpty.hidden = false;
            catContent.hidden = true;
            return;
        }

        catEmpty.hidden = true;
        catContent.hidden = false;

        filtered.forEach(c => {
            const tr = document.createElement('tr');

            // ID
            const tdId = document.createElement('td');
            tdId.textContent = c.id;
            tr.appendChild(tdId);

            // Name
            const tdName = document.createElement('td');
            tdName.textContent = c.name || '—';
            tdName.style.fontWeight = '600';
            tr.appendChild(tdName);

            // Description
            const tdDesc = document.createElement('td');
            tdDesc.textContent = c.description || '—';
            tr.appendChild(tdDesc);

            // Status
            const tdStatus = document.createElement('td');
            const sBadge = document.createElement('span');
            const isActive = c.status === 'ACTIVE';
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
            btnEdit.addEventListener('click', () => openEditModal(c));
            group.appendChild(btnEdit);

            const btnDelete = document.createElement('button');
            btnDelete.className = 'btn-action-sm btn-reject';
            btnDelete.textContent = 'Xóa';
            btnDelete.addEventListener('click', () => handleDeleteCategory(c.id, c.name));
            group.appendChild(btnDelete);

            tdActions.appendChild(group);
            tr.appendChild(tdActions);

            catTableBody.appendChild(tr);
        });
    };

    const loadCategories = async () => {
        catLoading.hidden = false;
        catEmpty.hidden = true;
        catContent.hidden = true;

        try {
            const res = await fetch('/api/categories');
            const payload = await res.json();
            if (!res.ok || !payload.success) throw new Error(payload.message || 'Lỗi tải danh mục.');

            allCategories = payload.data || [];
            renderTable();
        } catch (err) {
            showAlert(err.message, 'error');
            catEmpty.hidden = false;
        } finally {
            catLoading.hidden = true;
        }
    };

    const openCreateModal = () => {
        catIdInput.value = '';
        catModalTitle.textContent = 'Thêm danh mục sự cố';
        inputCatName.value = '';
        inputCatDesc.value = '';
        inputCatStatus.value = 'ACTIVE';
        catModal.hidden = false;
        inputCatName.focus();
    };

    const openEditModal = (c) => {
        catIdInput.value = c.id;
        catModalTitle.textContent = `Chỉnh sửa: ${c.name}`;
        inputCatName.value = c.name || '';
        inputCatDesc.value = c.description || '';
        inputCatStatus.value = c.status || 'ACTIVE';
        catModal.hidden = false;
        inputCatName.focus();
    };

    if (btnCloseModal) {
        btnCloseModal.addEventListener('click', () => { catModal.hidden = true; });
    }

    if (btnOpenCreate) {
        btnOpenCreate.addEventListener('click', openCreateModal);
    }

    catForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = catIdInput.value;
        const isEdit = Boolean(id);

        const payload = {
            name: inputCatName.value.trim(),
            description: inputCatDesc.value.trim(),
            status: inputCatStatus.value
        };

        const submitBtn = document.getElementById('btnCatModalSubmit');
        if (submitBtn) submitBtn.disabled = true;

        try {
            const url = isEdit ? `/api/categories/${id}` : '/api/categories';
            const method = isEdit ? 'PUT' : 'POST';

            const res = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                body: JSON.stringify(payload)
            });

            const data = await res.json();
            if (!res.ok || !data.success) throw new Error(data.message || 'Lưu danh mục thất bại.');

            catModal.hidden = true;
            showAlert(isEdit ? 'Cập nhật danh mục thành công!' : 'Tạo danh mục mới thành công!');
            loadCategories();
        } catch (err) {
            alert('Lỗi: ' + err.message);
        } finally {
            if (submitBtn) submitBtn.disabled = false;
        }
    });

    const handleDeleteCategory = async (id, name) => {
        if (!confirm(`Bạn có chắc chắn muốn xóa danh mục "${name}"?`)) return;

        try {
            const res = await fetch(`/api/categories/${encodeURIComponent(id)}`, { method: 'DELETE' });
            if (!res.ok) throw new Error('Xóa danh mục thất bại.');

            showAlert(`Đã xóa danh mục "${name}" thành công!`);
            loadCategories();
        } catch (err) {
            showAlert(err.message, 'error');
        }
    };

    if (catSearchInput) catSearchInput.addEventListener('input', renderTable);

    loadCategories();
});
