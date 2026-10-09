document.addEventListener('DOMContentLoaded', () => {
    // State Containers
    const editLoading = document.getElementById('editLoading');
    const editError = document.getElementById('editError');
    const alertTitle = document.getElementById('alertTitle');
    const alertMessage = document.getElementById('alertMessage');
    const retryButton = document.getElementById('retryButton');
    const backToListButton = document.getElementById('backToListButton');

    const editNotAllowedAlert = document.getElementById('editNotAllowedAlert');
    const notAllowedMessage = document.getElementById('notAllowedMessage');
    const notAllowedBackLink = document.getElementById('notAllowedBackLink');

    const formFeedback = document.getElementById('formFeedback');
    const form = document.getElementById('editRequestForm');
    const submitButton = document.getElementById('submitEditRequest');
    const cancelEditButton = document.getElementById('cancelEditButton');
    const backToDetailTopLink = document.getElementById('backToDetailTopLink');

    // Read-only Meta Elements
    const metaRequestCode = document.getElementById('metaRequestCode');
    const metaRequester = document.getElementById('metaRequester');
    const metaStatusBadge = document.getElementById('metaStatusBadge');

    // Form Field Elements
    const fields = {
        title: document.getElementById('title'),
        description: document.getElementById('description'),
        categoryId: document.getElementById('categoryId'),
        roomId: document.getElementById('roomId'),
        deviceId: document.getElementById('deviceId')
    };

    // Label dictionaries
    const statusLabels = {
        PENDING: 'Chờ tiếp nhận',
        RECEIVED: 'Đã tiếp nhận',
        ASSIGNED: 'Đã phân công',
        IN_PROGRESS: 'Đang xử lý',
        WAITING_INFO: 'Chờ bổ sung thông tin',
        COMPLETED: 'Đã hoàn thành',
        REJECTED: 'Từ chối',
        CANCELLED: 'Đã hủy'
    };

    const editableStatuses = ['PENDING', 'WAITING_INFO'];

    // Extract request ID from URL (/requests/{id}/edit)
    const getRequestIdFromPath = () => {
        const path = window.location.pathname;
        const match = path.match(/\/requests\/([^/?#]+)\/edit/);
        return match ? decodeURIComponent(match[1]) : null;
    };

    const requestId = getRequestIdFromPath();

    // UI state switchers
    const showLoadingState = () => {
        editLoading.hidden = false;
        editError.hidden = true;
        editNotAllowedAlert.hidden = true;
        form.hidden = true;
        formFeedback.hidden = true;
    };

    const show404State = () => {
        editLoading.hidden = true;
        form.hidden = true;
        editNotAllowedAlert.hidden = true;
        editError.hidden = false;
        editError.classList.add('alert-not-found');
        alertTitle.textContent = 'Không tìm thấy yêu cầu hỗ trợ.';
        alertMessage.textContent = 'Yêu cầu không tồn tại hoặc đã bị xóa khỏi hệ thống.';
        retryButton.hidden = true;
        backToListButton.hidden = false;
    };

    const showErrorState = (msg = 'Không thể tải thông tin yêu cầu. Vui lòng thử lại.') => {
        editLoading.hidden = true;
        form.hidden = true;
        editNotAllowedAlert.hidden = true;
        editError.hidden = false;
        editError.classList.remove('alert-not-found');
        alertTitle.textContent = 'Lỗi tải dữ liệu';
        alertMessage.textContent = msg;
        retryButton.hidden = false;
        backToListButton.hidden = false;
    };

    const showNotAllowedState = (statusKey, reqId) => {
        editLoading.hidden = true;
        form.hidden = true;
        editError.hidden = true;
        editNotAllowedAlert.hidden = false;

        const sLabel = statusLabels[statusKey] || statusKey || '—';
        notAllowedMessage.textContent = `Yêu cầu này đang ở trạng thái "${sLabel}" nên không được phép chỉnh sửa thông tin.`;
        const targetUrl = `/requests/${encodeURIComponent(reqId)}`;
        notAllowedBackLink.href = targetUrl;
        backToDetailTopLink.href = targetUrl;
    };

    const showFormState = () => {
        editLoading.hidden = true;
        editError.hidden = true;
        editNotAllowedAlert.hidden = true;
        form.hidden = false;
    };

    const showFeedback = (message, type) => {
        formFeedback.textContent = message;
        formFeedback.className = `form-feedback ${type}`;
        formFeedback.hidden = false;
        formFeedback.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    };

    // Validation helpers
    const setFieldError = (name, message = '') => {
        const error = document.querySelector(`[data-error-for="${name}"]`);
        const field = fields[name];
        if (error) error.textContent = message;
        field?.classList.toggle('has-error', Boolean(message));
    };

    const clearErrors = () => {
        Object.keys(fields).forEach((name) => setFieldError(name));
        setFieldError('priority');
    };

    // Populate dropdown options
    const loadSelectOptions = async (url, selectElement, emptyLabel, getOptionLabel) => {
        try {
            const res = await fetch(url, { headers: { Accept: 'application/json' } });
            const payload = await res.json();
            if (!res.ok || payload.success !== true || !Array.isArray(payload.data)) {
                throw new Error('Option fetch failed');
            }
            selectElement.replaceChildren(new Option(emptyLabel, ''));
            payload.data.forEach((item) => {
                selectElement.add(new Option(getOptionLabel(item), item.id));
            });
        } catch (err) {
            console.error(`Failed to load options from ${url}:`, err);
            selectElement.replaceChildren(new Option('Không thể tải danh sách', ''));
        }
    };

    // Pre-populate data into form
    const populateFormData = (req) => {
        const detailUrl = `/requests/${encodeURIComponent(req.id)}`;
        backToDetailTopLink.href = detailUrl;
        cancelEditButton.href = detailUrl;

        // Readonly meta
        metaRequestCode.textContent = req.requestCode || '—';
        metaRequester.textContent = req.requester?.fullName || '—';

        const sKey = req.status || '';
        const sLabel = statusLabels[sKey] || sKey || '—';
        metaStatusBadge.textContent = sLabel;
        metaStatusBadge.className = `request-status status-${String(sKey).toLowerCase()}`;

        // Editable inputs
        fields.title.value = req.title || '';
        fields.description.value = req.description || '';

        // Selects
        if (req.category?.id) {
            fields.categoryId.value = String(req.category.id);
        }
        if (req.room?.id) {
            fields.roomId.value = String(req.room.id);
        }
        if (req.device?.id) {
            fields.deviceId.value = String(req.device.id);
        }

        // Priority radio
        if (req.priority) {
            const radio = document.querySelector(`input[name="priority"][value="${req.priority}"]`);
            if (radio) radio.checked = true;
        }
    };

    // Client-side Validation
    const validateForm = () => {
        clearErrors();
        let isValid = true;

        const titleValue = fields.title.value.trim();
        if (!titleValue) {
            setFieldError('title', 'Vui lòng nhập tiêu đề yêu cầu.');
            isValid = false;
        } else if (titleValue.length > 200) {
            setFieldError('title', 'Tiêu đề tối đa 200 ký tự.');
            isValid = false;
        }

        if (!fields.categoryId.value) {
            setFieldError('categoryId', 'Vui lòng chọn danh mục sự cố.');
            isValid = false;
        }

        const selectedPriority = document.querySelector('input[name="priority"]:checked');
        if (!selectedPriority) {
            setFieldError('priority', 'Vui lòng chọn mức độ ưu tiên.');
            isValid = false;
        }

        return isValid;
    };

    // Main loader
    const loadAllData = async () => {
        if (!requestId) {
            show404State();
            return;
        }

        showLoadingState();

        try {
            // Load request detail and select options concurrently
            const [reqRes] = await Promise.all([
                fetch(`/api/requests/${encodeURIComponent(requestId)}`, {
                    headers: { Accept: 'application/json' }
                }),
                loadSelectOptions('/api/categories', fields.categoryId, 'Chọn danh mục sự cố', (item) => item.name),
                loadSelectOptions('/api/rooms', fields.roomId, 'Không chọn phòng', (item) => `${item.roomCode} - ${item.roomName}`),
                loadSelectOptions('/api/devices', fields.deviceId, 'Không chọn thiết bị', (item) => `${item.deviceCode} - ${item.deviceName}`)
            ]);

            if (reqRes.status === 404) {
                show404State();
                return;
            }

            const reqPayload = await reqRes.json();
            if (!reqRes.ok || reqPayload.success !== true || !reqPayload.data) {
                if (reqRes.status === 404 || reqPayload.message?.includes('không tìm thấy')) {
                    show404State();
                    return;
                }
                throw new Error(reqPayload.message || 'Lỗi tải yêu cầu');
            }

            const reqData = reqPayload.data;

            // Check if status is editable
            if (!editableStatuses.includes(reqData.status)) {
                showNotAllowedState(reqData.status, reqData.id);
                return;
            }

            // Populate form
            populateFormData(reqData);
            showFormState();
        } catch (err) {
            console.error('Failed to load edit request data:', err);
            showErrorState();
        }
    };

    // Submit handler
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        formFeedback.hidden = true;

        if (!validateForm()) {
            return;
        }

        const selectedPriority = document.querySelector('input[name="priority"]:checked')?.value;

        // Build body with only permitted fields: title, description, categoryId, roomId, deviceId, priority
        // Do NOT send status, requesterId, requestCode, createdAt, updatedAt
        const updateBody = {
            title: fields.title.value.trim(),
            description: fields.description.value.trim(),
            categoryId: Number(fields.categoryId.value),
            priority: selectedPriority
        };

        if (fields.roomId.value) {
            updateBody.roomId = Number(fields.roomId.value);
        }

        if (fields.deviceId.value) {
            updateBody.deviceId = Number(fields.deviceId.value);
        }

        // Disable submit button & update text
        submitButton.disabled = true;
        const originalButtonHtml = submitButton.innerHTML;
        submitButton.innerHTML = '<span class="loading-spinner compact" aria-hidden="true"></span> Đang lưu...';

        try {
            const res = await fetch(`/api/requests/${encodeURIComponent(requestId)}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(updateBody)
            });

            const payload = await res.json();

            if (!res.ok || payload.success !== true) {
                if (res.status === 400) {
                    throw new Error(payload.message || 'Dữ liệu không hợp lệ. Vui lòng kiểm tra lại thông tin.');
                } else if (res.status === 404) {
                    throw new Error('Không tìm thấy yêu cầu hỗ trợ hoặc danh mục/phòng/thiết bị liên quan.');
                } else {
                    throw new Error('Đã xảy ra lỗi hệ thống khi cập nhật yêu cầu. Vui lòng thử lại.');
                }
            }

            showFeedback('Cập nhật yêu cầu hỗ trợ thành công! Đang chuyển hướng...', 'success');

            // Redirect back to request detail after short delay
            setTimeout(() => {
                window.location.href = `/requests/${encodeURIComponent(requestId)}`;
            }, 600);
        } catch (err) {
            console.error('Failed to submit request update:', err);
            showFeedback(err.message || 'Không thể lưu thay đổi. Vui lòng thử lại.', 'error');
            submitButton.disabled = false;
            submitButton.innerHTML = originalButtonHtml;
        }
    });

    if (retryButton) {
        retryButton.addEventListener('click', loadAllData);
    }

    // Initial load
    loadAllData();
});
