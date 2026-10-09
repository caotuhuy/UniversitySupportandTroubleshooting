// ============================================================
// EAUT SUPPORT — Request Detail Script
// Đại học Công nghệ Đông Á
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
    // Elements
    const detailLoading = document.getElementById('detailLoading');
    const detailError = document.getElementById('detailError');
    const alertTitle = document.getElementById('alertTitle');
    const alertMessage = document.getElementById('alertMessage');
    const retryButton = document.getElementById('retryButton');
    const backToListButton = document.getElementById('backToListButton');
    const detailContent = document.getElementById('detailContent');

    // Header & Info elements
    const pageTitleHeading = document.getElementById('pageTitleHeading');
    const headerRequestCode = document.getElementById('headerRequestCode');
    const editRequestButton = document.getElementById('editRequestButton');
    const cancelRequestButton = document.getElementById('cancelRequestButton');
    const btnResumeRequest = document.getElementById('btnResumeRequest');
    const detailStatusBadge = document.getElementById('detailStatusBadge');
    const detailPriorityBadge = document.getElementById('detailPriorityBadge');

    const detailTitle = document.getElementById('detailTitle');
    const detailDescription = document.getElementById('detailDescription');
    const detailCategory = document.getElementById('detailCategory');
    const detailRequester = document.getElementById('detailRequester');
    const detailRoom = document.getElementById('detailRoom');
    const detailDevice = document.getElementById('detailDevice');
    const detailPriorityText = document.getElementById('detailPriorityText');
    const detailStatusText = document.getElementById('detailStatusText');
    const detailCreatedAt = document.getElementById('detailCreatedAt');
    const detailUpdatedAt = document.getElementById('detailUpdatedAt');
    const detailCompletedAt = document.getElementById('detailCompletedAt');
    const detailCancelledAt = document.getElementById('detailCancelledAt');
    const cancelledAtRow = document.getElementById('cancelledAtRow');

    // Cancel modal elements
    const cancelModalBackdrop = document.getElementById('cancelModalBackdrop');
    const btnCancelModalClose = document.getElementById('btnCancelModalClose');
    const btnConfirmCancel = document.getElementById('btnConfirmCancel');
    const cancelReasonInput = document.getElementById('cancelReasonInput');

    // Resume modal elements
    const resumeModalBackdrop = document.getElementById('resumeModalBackdrop');
    const resumeNoteInput = document.getElementById('resumeNoteInput');
    const btnConfirmResume = document.getElementById('btnConfirmResume');

    // Feedback elements
    const feedbackSection = document.getElementById('feedbackSection');
    const feedbackForm = document.getElementById('feedbackForm');
    const feedbackView = document.getElementById('feedbackView');
    const starBtns = document.querySelectorAll('.star-btn');
    const selectedRatingInput = document.getElementById('selectedRating');
    const starRatingText = document.getElementById('starRatingText');
    const feedbackComment = document.getElementById('feedbackComment');
    const btnSubmitFeedback = document.getElementById('btnSubmitFeedback');
    const feedbackDisplayStars = document.getElementById('feedbackDisplayStars');
    const feedbackDisplayComment = document.getElementById('feedbackDisplayComment');
    const feedbackDisplayDate = document.getElementById('feedbackDisplayDate');

    // History elements
    const historyLoading = document.getElementById('historyLoading');
    const historyEmpty = document.getElementById('historyEmpty');
    const historyError = document.getElementById('historyError');
    const historyTimeline = document.getElementById('historyTimeline');

    let currentUserId = null;
    let currentUserRoles = [];

    const priorityLabels = {
        LOW: 'Thấp',
        MEDIUM: 'Trung bình',
        HIGH: 'Cao',
        URGENT: 'Khẩn cấp'
    };

    const statusLabels = {
        PENDING: 'Chờ tiếp nhận',
        RECEIVED: 'Đã tiếp nhận',
        ASSIGNED: 'Đã phân công',
        IN_PROGRESS: 'Đang xử lý',
        WAITING_INFO: 'Chờ thông tin',
        COMPLETED: 'Đã hoàn thành',
        REJECTED: 'Từ chối',
        CANCELLED: 'Đã hủy'
    };

    const starRatingLabels = {
        1: '1 sao - Rất không hài lòng',
        2: '2 sao - Chưa hài lòng',
        3: '3 sao - Bình thường',
        4: '4 sao - Hài lòng',
        5: '5 sao - Rất hài lòng'
    };

    const formatDateTime = (value) => {
        if (!value) return null;
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return null;
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return `${day}/${month}/${year} ${hours}:${minutes}`;
    };

    const safeText = (value, fallback = '—') => {
        if (value === null || value === undefined || value === '') return fallback;
        return String(value);
    };

    const getRequestIdFromPath = () => {
        const path = window.location.pathname;
        const match = path.match(/\/requests\/([^/?#]+)/);
        return match ? decodeURIComponent(match[1]) : null;
    };

    const requestId = getRequestIdFromPath();

    // Fetch current user
    const fetchCurrentUser = async () => {
        try {
            const res = await fetch('/api/auth/me');
            if (res.ok) {
                const data = await res.json();
                if (data.success && data.data) {
                    currentUserId = data.data.id;
                    currentUserRoles = (data.data.roles || []).map(r => r.name);
                }
            }
        } catch (e) {
            console.error('Failed to get current user', e);
        }
    };
    fetchCurrentUser();

    const showLoadingState = () => {
        detailLoading.hidden = false;
        detailError.hidden = true;
        detailContent.hidden = true;
        if (editRequestButton) editRequestButton.hidden = true;
        if (cancelRequestButton) cancelRequestButton.hidden = true;
        if (btnResumeRequest) btnResumeRequest.hidden = true;
    };

    const show404State = () => {
        detailLoading.hidden = true;
        detailContent.hidden = true;
        detailError.hidden = false;
        alertTitle.textContent = 'Không tìm thấy yêu cầu hỗ trợ.';
        alertMessage.textContent = 'Yêu cầu không tồn tại hoặc đã bị xóa khỏi hệ thống.';
        retryButton.hidden = true;
        backToListButton.hidden = false;
        headerRequestCode.textContent = '—';
        if (editRequestButton) editRequestButton.hidden = true;
        if (cancelRequestButton) cancelRequestButton.hidden = true;
        if (btnResumeRequest) btnResumeRequest.hidden = true;
    };

    const showErrorState = () => {
        detailLoading.hidden = true;
        detailContent.hidden = true;
        detailError.hidden = false;
        alertTitle.textContent = 'Không thể tải thông tin yêu cầu.';
        alertMessage.textContent = 'Đã có lỗi xảy ra khi kết nối máy chủ. Vui lòng kiểm tra lại mạng.';
        retryButton.hidden = false;
        backToListButton.hidden = false;
        if (editRequestButton) editRequestButton.hidden = true;
        if (cancelRequestButton) cancelRequestButton.hidden = true;
        if (btnResumeRequest) btnResumeRequest.hidden = true;
    };

    const showSuccessState = () => {
        detailLoading.hidden = true;
        detailError.hidden = true;
        detailContent.hidden = false;
    };

    // Update workflow steps tracker
    const updateWorkflowTracker = (status) => {
        const stepCreated = document.getElementById('stepCreated');
        const stepReceived = document.getElementById('stepReceived');
        const stepAssigned = document.getElementById('stepAssigned');
        const stepInProgress = document.getElementById('stepInProgress');
        const stepCompleted = document.getElementById('stepCompleted');
        const sep1 = document.getElementById('sep1');
        const sep2 = document.getElementById('sep2');
        const sep3 = document.getElementById('sep3');
        const sep4 = document.getElementById('sep4');

        const allSteps = [stepCreated, stepReceived, stepAssigned, stepInProgress, stepCompleted];
        const allSeps = [sep1, sep2, sep3, sep4];

        allSteps.forEach(s => { if (s) s.className = 'workflow-step'; });
        allSeps.forEach(s => { if (s) s.className = 'workflow-sep'; });

        if (status === 'CANCELLED' || status === 'REJECTED') {
            if (stepCreated) {
                stepCreated.className = 'workflow-step completed';
                stepCreated.querySelector('.workflow-step-num').textContent = '✓';
            }
            if (stepReceived) {
                stepReceived.className = 'workflow-step';
                stepReceived.querySelector('.workflow-step-num').textContent = '✕';
                stepReceived.querySelector('span:last-child').textContent = status === 'CANCELLED' ? 'Đã hủy' : 'Từ chối';
            }
            return;
        }

        // Normal flow
        if (stepCreated) {
            stepCreated.className = 'workflow-step completed';
            stepCreated.querySelector('.workflow-step-num').textContent = '✓';
        }

        if (status === 'PENDING') {
            if (stepReceived) stepReceived.className = 'workflow-step active';
        } else if (status === 'RECEIVED') {
            if (sep1) sep1.className = 'workflow-sep active';
            if (stepReceived) {
                stepReceived.className = 'workflow-step completed';
                stepReceived.querySelector('.workflow-step-num').textContent = '✓';
            }
            if (stepAssigned) stepAssigned.className = 'workflow-step active';
        } else if (status === 'ASSIGNED') {
            if (sep1) sep1.className = 'workflow-sep active';
            if (sep2) sep2.className = 'workflow-sep active';
            if (stepReceived) {
                stepReceived.className = 'workflow-step completed';
                stepReceived.querySelector('.workflow-step-num').textContent = '✓';
            }
            if (stepAssigned) {
                stepAssigned.className = 'workflow-step completed';
                stepAssigned.querySelector('.workflow-step-num').textContent = '✓';
            }
            if (stepInProgress) stepInProgress.className = 'workflow-step active';
        } else if (status === 'IN_PROGRESS' || status === 'WAITING_INFO') {
            if (sep1) sep1.className = 'workflow-sep active';
            if (sep2) sep2.className = 'workflow-sep active';
            if (sep3) sep3.className = 'workflow-sep active';
            if (stepReceived) {
                stepReceived.className = 'workflow-step completed';
                stepReceived.querySelector('.workflow-step-num').textContent = '✓';
            }
            if (stepAssigned) {
                stepAssigned.className = 'workflow-step completed';
                stepAssigned.querySelector('.workflow-step-num').textContent = '✓';
            }
            if (stepInProgress) {
                stepInProgress.className = 'workflow-step active';
                if (status === 'WAITING_INFO') {
                    stepInProgress.querySelector('span:last-child').textContent = '4. Chờ thông tin';
                }
            }
        } else if (status === 'COMPLETED') {
            allSeps.forEach(s => { if (s) s.className = 'workflow-sep active'; });
            allSteps.forEach(s => {
                if (s) {
                    s.className = 'workflow-step completed';
                    s.querySelector('.workflow-step-num').textContent = '✓';
                }
            });
        }
    };

    // Render details
    const renderRequestDetail = (req) => {
        headerRequestCode.textContent = safeText(req.requestCode || ('REQ-' + req.id));
        pageTitleHeading.textContent = safeText(req.title, 'Chi tiết yêu cầu');

        detailTitle.textContent = safeText(req.title);
        detailDescription.textContent = safeText(req.description);

        const pKey = req.priority || '';
        const pLabel = priorityLabels[pKey] || safeText(pKey);
        detailPriorityText.textContent = pLabel;
        detailPriorityBadge.textContent = pLabel;
        detailPriorityBadge.className = `priority-badge priority-${String(pKey).toLowerCase()}`;

        const sKey = req.status || '';
        const sLabel = statusLabels[sKey] || safeText(sKey);
        detailStatusText.textContent = sLabel;
        detailStatusBadge.textContent = sLabel;
        detailStatusBadge.className = `request-status status-${String(sKey).toLowerCase()}`;

        detailCategory.textContent = safeText(req.category?.name);
        detailRequester.textContent = safeText(req.requester?.fullName);

        if (req.room) {
            const rCode = req.room.roomCode || '';
            const rName = req.room.roomName || '';
            detailRoom.textContent = rCode && rName ? `${rCode} - ${rName}` : safeText(rCode || rName);
        } else {
            detailRoom.textContent = '—';
        }

        if (req.device) {
            const dCode = req.device.deviceCode || '';
            const dName = req.device.deviceName || '';
            detailDevice.textContent = dCode && dName ? `${dCode} - ${dName}` : safeText(dCode || dName);
        } else {
            detailDevice.textContent = '—';
        }

        detailCreatedAt.textContent = formatDateTime(req.createdAt) || '—';
        detailUpdatedAt.textContent = formatDateTime(req.updatedAt) || '—';
        detailCompletedAt.textContent = req.completedAt ? (formatDateTime(req.completedAt) || 'Chưa hoàn thành') : 'Chưa hoàn thành';

        if (req.cancelledAt) {
            detailCancelledAt.textContent = formatDateTime(req.cancelledAt);
            if (cancelledAtRow) cancelledAtRow.hidden = false;
        } else {
            if (cancelledAtRow) cancelledAtRow.hidden = true;
        }

        const detailTechnician = document.getElementById('detailTechnician');
        if (detailTechnician) {
            detailTechnician.textContent = req.currentTechnician ? (req.currentTechnician.fullName || req.currentTechnician.username) : 'Chưa phân công';
        }

        // SLA
        const detailSlaDeadline = document.getElementById('detailSlaDeadline');
        const detailSlaStatusBadge = document.getElementById('detailSlaStatusBadge');
        if (detailSlaDeadline) {
            detailSlaDeadline.textContent = formatDateTime(req.resolutionDeadline) || 'Chưa thiết lập';
        }
        if (detailSlaStatusBadge) {
            if (req.isOverdue) {
                detailSlaStatusBadge.innerHTML = '<span class="sla-badge sla-overdue">Quá hạn SLA</span>';
            } else if (req.slaStatus === 'WARNING') {
                detailSlaStatusBadge.innerHTML = '<span class="sla-badge sla-warning">Sắp hết hạn</span>';
            } else if (req.status === 'COMPLETED') {
                detailSlaStatusBadge.innerHTML = '<span class="sla-badge sla-ok">Hoàn thành đúng hạn</span>';
            } else {
                detailSlaStatusBadge.innerHTML = '<span class="sla-badge sla-ok">Trong hạn cam kết</span>';
            }
        }

        // Update workflow visual tracker
        updateWorkflowTracker(req.status);

        // Edit button: Only PENDING and WAITING_INFO
        if (editRequestButton) {
            const editableStatuses = ['PENDING', 'WAITING_INFO'];
            if (editableStatuses.includes(req.status)) {
                editRequestButton.href = `/requests/${encodeURIComponent(req.id)}/edit`;
                editRequestButton.hidden = false;
            } else {
                editRequestButton.hidden = true;
            }
        }

        // Cancel button: Only before completion
        if (cancelRequestButton) {
            const cancellableStatuses = ['PENDING', 'RECEIVED', 'ASSIGNED', 'IN_PROGRESS', 'WAITING_INFO'];
            if (cancellableStatuses.includes(req.status)) {
                cancelRequestButton.hidden = false;
            } else {
                cancelRequestButton.hidden = true;
            }
        }

        // Resume button: if WAITING_INFO
        if (btnResumeRequest) {
            if (req.status === 'WAITING_INFO') {
                btnResumeRequest.hidden = false;
            } else {
                btnResumeRequest.hidden = true;
            }
        }

        // Feedback Section
        if (req.status === 'COMPLETED') {
            loadFeedback(req.id);
        } else {
            if (feedbackSection) feedbackSection.hidden = true;
        }
    };

    // Feedback logic
    const loadFeedback = async (id) => {
        if (!feedbackSection) return;
        feedbackSection.hidden = false;

        try {
            const res = await fetch(`/api/requests/${encodeURIComponent(id)}/feedback`);
            if (res.ok) {
                const payload = await res.json();
                if (payload.success && payload.data) {
                    const fb = payload.data;
                    if (feedbackForm) feedbackForm.hidden = true;
                    if (feedbackView) {
                        feedbackView.hidden = false;
                        const rating = fb.rating || 5;
                        feedbackDisplayStars.textContent = '★'.repeat(rating) + '☆'.repeat(5 - rating);
                        feedbackDisplayComment.textContent = safeText(fb.comment, '(Không có nhận xét thêm)');
                        feedbackDisplayDate.textContent = 'Đã gửi đánh giá: ' + (formatDateTime(fb.createdAt) || '—');
                    }
                    return;
                }
            }
        } catch (e) {
            // No feedback yet
        }

        if (feedbackForm) feedbackForm.hidden = false;
        if (feedbackView) feedbackView.hidden = true;
    };

    // Star rating
    starBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const val = parseInt(btn.dataset.value, 10);
            if (selectedRatingInput) selectedRatingInput.value = val;
            if (starRatingText) starRatingText.textContent = starRatingLabels[val] || `${val} sao`;

            starBtns.forEach(b => {
                const bVal = parseInt(b.dataset.value, 10);
                b.classList.toggle('active', bVal <= val);
            });
        });
    });

    if (feedbackForm) {
        feedbackForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            if (btnSubmitFeedback) btnSubmitFeedback.disabled = true;

            const rating = parseInt(selectedRatingInput?.value || '5', 10);
            const comment = feedbackComment?.value.trim() || '';

            try {
                const res = await fetch(`/api/requests/${encodeURIComponent(requestId)}/feedback`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                    body: JSON.stringify({ rating, comment })
                });

                const payload = await res.json();
                if (!res.ok || !payload.success) {
                    throw new Error(payload.message || 'Gửi đánh giá thất bại');
                }

                if (window.showToast) window.showToast('success', 'Đánh giá thành công', 'Cảm ơn bạn đã phản hồi chất lượng dịch vụ!');
                loadFeedback(requestId);
            } catch (err) {
                if (window.showToast) window.showToast('error', 'Lỗi', err.message);
                else alert('Lỗi: ' + err.message);
            } finally {
                if (btnSubmitFeedback) btnSubmitFeedback.disabled = false;
            }
        });
    }

    // Cancel modal
    if (cancelRequestButton) {
        cancelRequestButton.addEventListener('click', () => {
            if (cancelModalBackdrop) {
                cancelReasonInput.value = '';
                cancelModalBackdrop.hidden = false;
                cancelReasonInput.focus();
            }
        });
    }

    if (btnConfirmCancel) {
        btnConfirmCancel.addEventListener('click', async () => {
            const reason = cancelReasonInput.value.trim() || 'Người dùng hủy yêu cầu.';
            btnConfirmCancel.disabled = true;

            try {
                const res = await fetch(`/api/requests/${encodeURIComponent(requestId)}/cancel`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                    body: JSON.stringify({ reason })
                });

                const payload = await res.json();
                if (!res.ok || !payload.success) {
                    throw new Error(payload.message || 'Hủy yêu cầu thất bại.');
                }

                if (cancelModalBackdrop) cancelModalBackdrop.hidden = true;
                if (window.showToast) window.showToast('success', 'Đã hủy yêu cầu', 'Yêu cầu hỗ trợ đã được chuyển sang trạng thái Đã hủy.');
                loadRequestDetail();
            } catch (err) {
                if (window.showToast) window.showToast('error', 'Lỗi', err.message);
                else alert('Lỗi: ' + err.message);
            } finally {
                btnConfirmCancel.disabled = false;
            }
        });
    }

    // Resume modal (WAITING_INFO -> IN_PROGRESS)
    if (btnResumeRequest) {
        btnResumeRequest.addEventListener('click', () => {
            if (resumeModalBackdrop) {
                resumeNoteInput.value = '';
                resumeModalBackdrop.hidden = false;
                resumeNoteInput.focus();
            }
        });
    }

    if (btnConfirmResume) {
        btnConfirmResume.addEventListener('click', async () => {
            const note = resumeNoteInput.value.trim();
            if (!note) {
                if (window.showToast) window.showToast('warning', 'Thiếu thông tin', 'Vui lòng nhập nội dung phản hồi bổ sung.');
                resumeNoteInput.focus();
                return;
            }

            btnConfirmResume.disabled = true;
            try {
                const res = await fetch(`/api/requests/${encodeURIComponent(requestId)}/resume`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                    body: JSON.stringify({ note })
                });

                const payload = await res.json();
                if (!res.ok || !payload.success) {
                    throw new Error(payload.message || 'Gửi phản hồi thất bại.');
                }

                if (resumeModalBackdrop) resumeModalBackdrop.hidden = true;
                if (window.showToast) window.showToast('success', 'Thành công', 'Đã bổ sung thông tin! Yêu cầu chuyển về trạng thái Đang xử lý.');
                loadRequestDetail();
            } catch (err) {
                if (window.showToast) window.showToast('error', 'Lỗi', err.message);
                else alert('Lỗi: ' + err.message);
            } finally {
                btnConfirmResume.disabled = false;
            }
        });
    }

    // Render Timeline History
    const renderHistory = (historyItems) => {
        historyLoading.hidden = true;
        historyTimeline.innerHTML = '';

        if (!Array.isArray(historyItems) || historyItems.length === 0) {
            historyEmpty.hidden = false;
            historyTimeline.hidden = true;
            historyError.hidden = true;
            return;
        }

        historyEmpty.hidden = true;
        historyError.hidden = true;
        historyTimeline.hidden = false;

        historyItems.forEach((item) => {
            const timelineItem = document.createElement('div');
            timelineItem.className = 'timeline-item';

            const marker = document.createElement('div');
            marker.className = 'timeline-marker';
            timelineItem.appendChild(marker);

            const card = document.createElement('div');
            card.className = 'timeline-card';

            const topRow = document.createElement('div');
            topRow.className = 'timeline-top';

            const actionTitle = document.createElement('span');
            actionTitle.className = 'timeline-action';
            actionTitle.textContent = safeText(item.action, 'Hoạt động xử lý');
            topRow.appendChild(actionTitle);

            const timeLabel = document.createElement('span');
            timeLabel.className = 'timeline-time';
            timeLabel.textContent = formatDateTime(item.createdAt) || '—';
            topRow.appendChild(timeLabel);

            card.appendChild(topRow);

            if (item.user && item.user.fullName) {
                const userRow = document.createElement('div');
                userRow.className = 'timeline-user';
                userRow.textContent = 'Người thực hiện: ';
                const userName = document.createElement('strong');
                userName.textContent = item.user.fullName;
                userRow.appendChild(userName);
                card.appendChild(userRow);
            }

            if (item.oldStatus || item.newStatus) {
                const statusRow = document.createElement('div');
                statusRow.className = 'timeline-status-transition';

                if (item.oldStatus) {
                    const oldBadge = document.createElement('span');
                    oldBadge.className = `request-status status-${String(item.oldStatus).toLowerCase()}`;
                    oldBadge.textContent = statusLabels[item.oldStatus] || item.oldStatus;
                    statusRow.appendChild(oldBadge);

                    const arrow = document.createElement('span');
                    arrow.className = 'timeline-arrow';
                    arrow.textContent = '→';
                    statusRow.appendChild(arrow);
                }

                if (item.newStatus) {
                    const newBadge = document.createElement('span');
                    newBadge.className = `request-status status-${String(item.newStatus).toLowerCase()}`;
                    newBadge.textContent = statusLabels[item.newStatus] || item.newStatus;
                    statusRow.appendChild(newBadge);
                }

                card.appendChild(statusRow);
            }

            if (item.note && item.note.trim()) {
                const noteBox = document.createElement('div');
                noteBox.className = 'timeline-note';
                noteBox.textContent = item.note.trim();
                card.appendChild(noteBox);
            }

            timelineItem.appendChild(card);
            historyTimeline.appendChild(timelineItem);
        });
    };

    const loadHistory = async (id) => {
        historyLoading.hidden = false;
        historyEmpty.hidden = true;
        historyError.hidden = true;
        historyTimeline.hidden = true;

        try {
            const res = await fetch(`/api/requests/${encodeURIComponent(id)}/history`, {
                headers: { Accept: 'application/json' }
            });
            const payload = await res.json();
            if (!res.ok || payload.success !== true) throw new Error();
            renderHistory(payload.data);
        } catch (err) {
            historyLoading.hidden = true;
            historyError.hidden = false;
        }
    };

    // Attachments
    const loadAttachments = async (reqId) => {
        const loading = document.getElementById('attachmentsLoading');
        const empty = document.getElementById('attachmentsEmpty');
        const list = document.getElementById('attachmentsList');
        if (!list) return;

        try {
            const res = await fetch(`/api/requests/${reqId}/attachments`);
            const payload = await res.json();
            if (!res.ok || !payload.success) throw new Error();

            const items = payload.data || [];
            if (loading) loading.hidden = true;

            if (items.length === 0) {
                if (empty) empty.hidden = false;
                list.hidden = true;
                return;
            }

            if (empty) empty.hidden = true;
            list.hidden = false;
            list.innerHTML = '';

            items.forEach(att => {
                const li = document.createElement('li');
                li.className = 'attachment-item';

                const sizeKb = att.fileSize ? Math.round(att.fileSize / 1024) : 0;
                const infoDiv = document.createElement('div');
                infoDiv.className = 'attachment-info';

                // File icon based on extension
                const ext = (att.fileName || '').split('.').pop().toLowerCase();
                let iconSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>';
                if (['png', 'jpg', 'jpeg', 'gif', 'webp'].includes(ext)) {
                    iconSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>';
                }

                infoDiv.innerHTML = `
                    <span class="attachment-icon">${iconSvg}</span>
                    <a class="attachment-name" href="${att.downloadUrl}" target="_blank" download="${att.fileName}" title="Tải xuống ${att.fileName}">${att.fileName}</a>
                    <span class="attachment-meta">${sizeKb} KB • ${att.uploadedByName || 'Người dùng'}</span>
                `;
                li.appendChild(infoDiv);

                const actionsDiv = document.createElement('div');
                actionsDiv.className = 'attachment-actions';

                const downloadBtn = document.createElement('a');
                downloadBtn.className = 'btn btn-sm btn-ghost';
                downloadBtn.href = att.downloadUrl;
                downloadBtn.setAttribute('download', att.fileName);
                downloadBtn.title = 'Tải xuống';
                downloadBtn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>';
                actionsDiv.appendChild(downloadBtn);

                const btnDel = document.createElement('button');
                btnDel.type = 'button';
                btnDel.className = 'btn btn-sm btn-ghost';
                btnDel.style.color = '#ef4444';
                btnDel.title = 'Xóa tệp';
                btnDel.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>';
                btnDel.addEventListener('click', async () => {
                    if (!confirm(`Bạn có chắc chắn muốn xóa tệp "${att.fileName}"?`)) return;
                    try {
                        const delRes = await fetch(`/api/requests/${reqId}/attachments/${att.id}`, { method: 'DELETE' });
                        if (delRes.ok) {
                            if (window.showToast) window.showToast('success', 'Đã xóa', `Tệp ${att.fileName} đã được xóa.`);
                            loadAttachments(reqId);
                        } else {
                            if (window.showToast) window.showToast('error', 'Lỗi', 'Không có quyền xóa tệp này.');
                        }
                    } catch (e) {
                        alert('Lỗi: ' + e.message);
                    }
                });
                actionsDiv.appendChild(btnDel);

                li.appendChild(actionsDiv);
                list.appendChild(li);
            });
        } catch {
            if (loading) loading.textContent = 'Không tải được tệp đính kèm.';
        }
    };

    // Upload attachment form
    const attachmentUploadForm = document.getElementById('attachmentUploadForm');
    const attachmentFileInput = document.getElementById('attachmentFileInput');
    if (attachmentUploadForm) {
        attachmentUploadForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            if (!attachmentFileInput.files || attachmentFileInput.files.length === 0) {
                if (window.showToast) window.showToast('warning', 'Chưa chọn tệp', 'Vui lòng chọn một tệp để tải lên.');
                return;
            }

            const formData = new FormData();
            formData.append('file', attachmentFileInput.files[0]);

            const submitBtn = document.getElementById('btnUploadAttachment');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Đang tải lên...';
            }

            try {
                const res = await fetch(`/api/requests/${requestId}/attachments`, {
                    method: 'POST',
                    body: formData
                });
                const payload = await res.json();
                if (!res.ok || !payload.success) throw new Error(payload.message || 'Tải lên tệp thất bại.');

                attachmentFileInput.value = '';
                if (window.showToast) window.showToast('success', 'Thành công', 'Đã tải lên tệp đính kèm!');
                loadAttachments(requestId);
            } catch (err) {
                if (window.showToast) window.showToast('error', 'Lỗi', err.message);
                else alert('Lỗi: ' + err.message);
            } finally {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg> Tải lên tệp';
                }
            }
        });
    }

    const loadRequestDetail = async () => {
        if (!requestId) {
            show404State();
            return;
        }

        showLoadingState();

        try {
            const res = await fetch(`/api/requests/${encodeURIComponent(requestId)}`, {
                headers: { Accept: 'application/json' }
            });

            if (res.status === 404) {
                show404State();
                return;
            }

            const payload = await res.json();
            if (!res.ok || payload.success !== true || !payload.data) {
                if (res.status === 404 || payload.message?.includes('không tìm thấy')) {
                    show404State();
                    return;
                }
                throw new Error(payload.message || 'API error');
            }

            renderRequestDetail(payload.data);
            showSuccessState();

            loadHistory(requestId);
            loadAttachments(requestId);
        } catch (err) {
            console.error('Failed to load request detail:', err);
            showErrorState();
        }
    };

    if (retryButton) retryButton.addEventListener('click', loadRequestDetail);

    loadRequestDetail();
});
