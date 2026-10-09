// JavaScript cho trang Thông báo
document.addEventListener('DOMContentLoaded', () => {
    let currentUserId = null;
    let allNotifications = [];
    let currentFilter = 'ALL';

    const notifLoading = document.getElementById('notifLoading');
    const notifEmpty = document.getElementById('notifEmpty');
    const notifList = document.getElementById('notifList');
    const notifAlert = document.getElementById('notifAlert');
    const filterChips = document.querySelectorAll('.filter-chip');
    const btnRefresh = document.getElementById('btnRefreshNotifs');

    const formatDateTime = (val) => {
        if (!val) return '—';
        const d = new Date(val);
        if (Number.isNaN(d.getTime())) return '—';
        return `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    };

    const showAlert = (msg, type = 'success') => {
        if (!notifAlert) return;
        notifAlert.className = `tech-alert ${type}`;
        notifAlert.textContent = msg;
        notifAlert.style.display = 'block';
        setTimeout(() => { notifAlert.style.display = 'none'; }, 4000);
    };

    const updateCounts = () => {
        const unread = allNotifications.filter(n => !n.isRead).length;
        const read = allNotifications.length - unread;

        document.getElementById('countAllNotifs').textContent = allNotifications.length;
        document.getElementById('countUnreadNotifs').textContent = unread;
        document.getElementById('countReadNotifs').textContent = read;
    };

    const getIconForType = (type) => {
        switch (type) {
            case 'WARNING': return '⚠';
            case 'DANGER': return '✕';
            case 'SUCCESS': return '✓';
            case 'ASSIGNMENT': return '⚡';
            default: return 'ℹ';
        }
    };

    const renderList = () => {
        notifList.innerHTML = '';

        const filtered = allNotifications.filter(n => {
            if (currentFilter === 'ALL') return true;
            if (currentFilter === 'UNREAD') return !n.isRead;
            if (currentFilter === 'READ') return n.isRead;
            return true;
        });

        if (filtered.length === 0) {
            notifEmpty.hidden = false;
            notifList.hidden = true;
            return;
        }

        notifEmpty.hidden = true;
        notifList.hidden = false;

        filtered.forEach(notif => {
            const card = document.createElement('div');
            card.className = `notif-card ${notif.isRead ? 'read' : 'unread'}`;

            // Icon
            const icon = document.createElement('div');
            const type = notif.type || 'INFO';
            icon.className = `notif-icon ${type}`;
            icon.textContent = getIconForType(type);
            card.appendChild(icon);

            // Body
            const body = document.createElement('div');
            body.className = 'notif-body';

            const header = document.createElement('div');
            header.className = 'notif-header';

            const title = document.createElement('h3');
            title.className = 'notif-title';
            title.textContent = notif.title || 'Thông báo';
            header.appendChild(title);

            const time = document.createElement('span');
            time.className = 'notif-time';
            time.textContent = formatDateTime(notif.createdAt);
            header.appendChild(time);

            body.appendChild(header);

            const content = document.createElement('p');
            content.className = 'notif-content';
            content.textContent = notif.content || '';
            body.appendChild(content);

            if (!notif.isRead) {
                const actions = document.createElement('div');
                actions.className = 'notif-actions';

                const btnRead = document.createElement('button');
                btnRead.type = 'button';
                btnRead.className = 'btn-mark-read';
                btnRead.textContent = 'Đánh dấu đã đọc';
                btnRead.addEventListener('click', () => handleMarkAsRead(notif.id));
                actions.appendChild(btnRead);

                body.appendChild(actions);
            }

            card.appendChild(body);
            notifList.appendChild(card);
        });
    };

    const loadNotifications = async () => {
        notifLoading.hidden = false;
        notifEmpty.hidden = true;
        notifList.hidden = true;

        try {
            if (!currentUserId) {
                const meRes = await fetch('/api/auth/me');
                if (meRes.status === 401) {
                    window.location.href = '/login';
                    return;
                }
                const meData = await meRes.json();
                if (!meData.success || !meData.data) throw new Error('Không lấy được thông tin người dùng.');
                currentUserId = meData.data.id;
            }

            const res = await fetch(`/api/notifications/user/${currentUserId}`, {
                headers: { Accept: 'application/json' }
            });
            const payload = await res.json();
            if (!res.ok || !payload.success) throw new Error(payload.message || 'Lỗi tải thông báo.');

            allNotifications = payload.data || [];
            updateCounts();
            renderList();
        } catch (err) {
            showAlert(err.message, 'error');
            notifEmpty.hidden = false;
        } finally {
            notifLoading.hidden = true;
        }
    };

    const handleMarkAsRead = async (id) => {
        try {
            const res = await fetch(`/api/notifications/${encodeURIComponent(id)}/read`, {
                method: 'PUT',
                headers: { Accept: 'application/json' }
            });
            const payload = await res.json();
            if (!res.ok || !payload.success) throw new Error(payload.message || 'Lỗi cập nhật trạng thái.');

            // Cập nhật local state
            const target = allNotifications.find(n => n.id === id);
            if (target) target.isRead = true;
            updateCounts();
            renderList();

            // Cập nhật badge trên header
            const headerBadge = document.getElementById('headerNotificationBadge');
            if (headerBadge) {
                const unread = allNotifications.filter(n => !n.isRead).length;
                if (unread > 0) {
                    headerBadge.textContent = unread > 99 ? '99+' : unread;
                    headerBadge.style.display = 'grid';
                } else {
                    headerBadge.style.display = 'none';
                }
            }
        } catch (err) {
            showAlert(err.message, 'error');
        }
    };

    filterChips.forEach(chip => {
        chip.addEventListener('click', () => {
            filterChips.forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            currentFilter = chip.dataset.filter;
            renderList();
        });
    });

    if (btnRefresh) {
        btnRefresh.addEventListener('click', loadNotifications);
    }

    loadNotifications();
});
