// JavaScript Quản lý Đánh giá & Phản hồi cho Admin
document.addEventListener('DOMContentLoaded', () => {
    let allFeedbacks = [];

    const fbLoading = document.getElementById('fbLoading');
    const fbEmpty = document.getElementById('fbEmpty');
    const fbContent = document.getElementById('fbContent');
    const fbTableBody = document.getElementById('fbTableBody');
    const fbAlert = document.getElementById('fbAlert');

    const avgRatingDisplay = document.getElementById('avgRatingDisplay');
    const totalFeedbacksDisplay = document.getElementById('totalFeedbacksDisplay');
    const positiveFeedbacksDisplay = document.getElementById('positiveFeedbacksDisplay');
    const negativeFeedbacksDisplay = document.getElementById('negativeFeedbacksDisplay');
    const ratingFilter = document.getElementById('ratingFilter');

    const showAlert = (msg, type = 'success') => {
        if (!fbAlert) return;
        fbAlert.className = `tech-alert ${type}`;
        fbAlert.textContent = msg;
        fbAlert.style.display = 'block';
        window.scrollTo({ top: 0, behavior: 'smooth' });
        setTimeout(() => { fbAlert.style.display = 'none'; }, 4000);
    };

    const formatDateTime = (isoStr) => {
        if (!isoStr) return '-';
        const d = new Date(isoStr);
        return d.toLocaleString('vi-VN', {
            hour: '2-digit', minute: '2-digit',
            day: '2-digit', month: '2-digit', year: 'numeric'
        });
    };

    const renderStars = (rating) => {
        const full = '★'.repeat(rating || 0);
        const empty = '☆'.repeat(5 - (rating || 0));
        return `<span class="star-rating">${full}${empty}</span> (${rating}/5)`;
    };

    const updateStats = (list) => {
        if (!list || list.length === 0) {
            avgRatingDisplay.textContent = '0.0 ★';
            totalFeedbacksDisplay.textContent = '0';
            positiveFeedbacksDisplay.textContent = '0';
            negativeFeedbacksDisplay.textContent = '0';
            return;
        }

        const total = list.length;
        const sum = list.reduce((acc, cur) => acc + (cur.rating || 0), 0);
        const avg = (sum / total).toFixed(1);
        const positive = list.filter(f => f.rating >= 4).length;
        const negative = list.filter(f => f.rating <= 2).length;

        avgRatingDisplay.textContent = `${avg} ★`;
        totalFeedbacksDisplay.textContent = total;
        positiveFeedbacksDisplay.textContent = positive;
        negativeFeedbacksDisplay.textContent = negative;
    };

    const renderTable = () => {
        fbTableBody.innerHTML = '';

        const selectedRating = ratingFilter.value;
        const filtered = selectedRating === 'ALL'
            ? allFeedbacks
            : allFeedbacks.filter(f => f.rating === parseInt(selectedRating, 10));

        if (filtered.length === 0) {
            fbEmpty.hidden = false;
            fbContent.hidden = true;
            return;
        }

        fbEmpty.hidden = true;
        fbContent.hidden = false;

        filtered.forEach(fb => {
            const tr = document.createElement('tr');

            // Mã sự cố
            const tdCode = document.createElement('td');
            const codeLink = document.createElement('a');
            codeLink.href = `/requests/${fb.requestId}`;
            codeLink.style.fontWeight = '600';
            codeLink.style.color = '#1e40af';
            codeLink.textContent = fb.requestCode || `#${fb.requestId}`;
            tdCode.appendChild(codeLink);
            tr.appendChild(tdCode);

            // Tiêu đề yêu cầu
            const tdTitle = document.createElement('td');
            tdTitle.textContent = fb.requestTitle || `Yêu cầu #${fb.requestId}`;
            tr.appendChild(tdTitle);

            // Người đánh giá
            const tdUser = document.createElement('td');
            const userName = fb.user ? (fb.user.fullName || fb.user.username) : 'Người dùng ẩn danh';
            const userSub = fb.user && fb.user.studentCode ? ` (${fb.user.studentCode})` : '';
            tdUser.textContent = userName + userSub;
            tr.appendChild(tdUser);

            // Số sao
            const tdRating = document.createElement('td');
            tdRating.innerHTML = renderStars(fb.rating);
            tr.appendChild(tdRating);

            // Nhận xét
            const tdComment = document.createElement('td');
            tdComment.textContent = fb.comment || 'Không có nhận xét';
            if (!fb.comment) {
                tdComment.style.color = '#94a3b8';
                tdComment.style.fontStyle = 'italic';
            }
            tr.appendChild(tdComment);

            // Thời gian
            const tdTime = document.createElement('td');
            tdTime.textContent = formatDateTime(fb.createdAt);
            tr.appendChild(tdTime);

            // Hành động
            const tdActions = document.createElement('td');
            const group = document.createElement('div');
            group.className = 'btn-action-group';

            const btnView = document.createElement('a');
            btnView.className = 'btn-action-sm btn-view';
            btnView.href = `/requests/${fb.requestId}`;
            btnView.textContent = 'Chi tiết';
            group.appendChild(btnView);

            const btnDelete = document.createElement('button');
            btnDelete.className = 'btn-action-sm btn-reject';
            btnDelete.textContent = 'Xóa';
            btnDelete.addEventListener('click', () => handleDeleteFeedback(fb.id));
            group.appendChild(btnDelete);

            tdActions.appendChild(group);
            tr.appendChild(tdActions);

            fbTableBody.appendChild(tr);
        });
    };

    const loadFeedbacks = async () => {
        fbLoading.hidden = false;
        fbEmpty.hidden = true;
        fbContent.hidden = true;

        try {
            const res = await fetch('/api/feedbacks');
            const payload = await res.json();
            if (!res.ok || !payload.success) throw new Error(payload.message || 'Lỗi tải danh sách đánh giá.');

            allFeedbacks = payload.data || [];
            updateStats(allFeedbacks);
            renderTable();
        } catch (err) {
            showAlert(err.message, 'error');
            fbEmpty.hidden = false;
        } finally {
            fbLoading.hidden = true;
        }
    };

    const handleDeleteFeedback = async (id) => {
        if (!confirm('Bạn có chắc chắn muốn xóa bản ghi đánh giá này?')) return;

        try {
            const res = await fetch(`/api/feedbacks/${id}`, { method: 'DELETE' });
            if (!res.ok) throw new Error('Xóa đánh giá thất bại.');

            showAlert('Đã xóa đánh giá thành công!');
            loadFeedbacks();
        } catch (err) {
            showAlert(err.message, 'error');
        }
    };

    ratingFilter.addEventListener('change', renderTable);

    loadFeedbacks();
});
