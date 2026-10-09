// ============================================================
// EAUT SUPPORT — Create Request Script
// Đại học Công nghệ Đông Á
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('createRequestForm');
    const feedback = document.getElementById('formFeedback');
    const submitButton = document.getElementById('submitRequest');

    const fields = {
        title: document.getElementById('title'),
        description: document.getElementById('description'),
        categoryId: document.getElementById('categoryId'),
        requesterId: document.getElementById('requesterId'),
        roomId: document.getElementById('roomId'),
        deviceId: document.getElementById('deviceId')
    };

    // Live preview elements
    const previewTitle = document.getElementById('previewTitle');
    const previewCategory = document.getElementById('previewCategory');
    const previewRoom = document.getElementById('previewRoom');
    const previewDevice = document.getElementById('previewDevice');
    const previewPriority = document.getElementById('previewPriority');
    const previewFileField = document.getElementById('previewFileField');
    const previewFileVal = document.getElementById('previewFileVal');

    // Dropzone elements
    const fileDropzone = document.getElementById('fileDropzone');
    const attachmentInput = document.getElementById('attachmentInput');
    const filePreviewWrap = document.getElementById('filePreviewWrap');
    const previewFileName = document.getElementById('previewFileName');
    const previewFileSize = document.getElementById('previewFileSize');
    const btnRemoveFile = document.getElementById('btnRemoveFile');

    let currentUser = null;
    let allDevices = [];
    let selectedFile = null;

    const PRIORITY_BADGES = {
        LOW:    '<span class="badge badge-priority-low"><span class="badge-dot"></span>Thấp</span>',
        MEDIUM: '<span class="badge badge-priority-medium"><span class="badge-dot"></span>Trung bình</span>',
        HIGH:   '<span class="badge badge-priority-high"><span class="badge-dot"></span>Cao</span>',
        URGENT: '<span class="badge badge-priority-urgent"><span class="badge-dot"></span>Khẩn cấp</span>'
    };

    const formatBytes = (bytes, decimals = 1) => {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const dm = decimals < 0 ? 0 : decimals;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
    };

    const setFieldError = (name, message = '') => {
        const errorEl = document.querySelector(`[data-error-for="${name}"]`);
        const fieldEl = fields[name];
        if (errorEl) {
            errorEl.textContent = message;
            errorEl.style.display = message ? 'block' : 'none';
        }
        if (fieldEl) {
            fieldEl.classList.toggle('has-error', Boolean(message));
        }
    };

    const clearErrors = () => {
        Object.keys(fields).forEach((name) => setFieldError(name));
        setFieldError('priority');
    };

    const showFeedback = (message, type) => {
        if (!feedback) return;
        feedback.textContent = message;
        feedback.className = `alert alert-${type}`;
        feedback.hidden = false;
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    // Load dropdown options
    const loadOptions = async (url, select, emptyLabel, getLabel) => {
        try {
            const response = await fetch(url, { headers: { Accept: 'application/json' } });
            const payload = await response.json();
            if (!response.ok || payload.success !== true || !Array.isArray(payload.data)) {
                throw new Error('Option request failed');
            }
            select.replaceChildren(new Option(emptyLabel, ''));
            payload.data.forEach((item) => select.add(new Option(getLabel(item), item.id)));
            return payload.data;
        } catch (error) {
            select.replaceChildren(new Option('Không thể tải dữ liệu', ''));
            select.disabled = true;
            return [];
        }
    };

    // Current user identification
    const initRequesterField = async () => {
        const requesterSelect = fields.requesterId;
        const helpText = document.getElementById('requesterHelp');

        try {
            const res = await fetch('/api/auth/me', { headers: { Accept: 'application/json' } });
            if (!res.ok) throw new Error('Chưa đăng nhập');
            const data = await res.json();
            if (!data.success || !data.data) throw new Error('Không lấy được thông tin');

            currentUser = data.data;
            const isAdmin = currentUser.roles?.some(r => r.name === 'ROLE_ADMIN');
            const isTechnician = currentUser.roles?.some(r => r.name === 'ROLE_TECHNICIAN');

            if (isAdmin || isTechnician) {
                if (helpText) helpText.textContent = 'Bạn có quyền tạo yêu cầu thay cho người khác.';
                await loadOptions(
                    '/api/users',
                    requesterSelect,
                    '-- Chọn người yêu cầu --',
                    (item) => `${item.fullName} (${item.username})`
                );
                requesterSelect.value = currentUser.id;
            } else {
                requesterSelect.innerHTML = `<option value="${currentUser.id}" selected>${currentUser.fullName} (${currentUser.username})</option>`;
                requesterSelect.disabled = true;
                requesterSelect.style.background = 'var(--bg-subtle)';
                requesterSelect.style.cursor = 'not-allowed';
                if (helpText) helpText.textContent = 'Yêu cầu được gửi từ tài khoản của bạn.';

                let hiddenInput = document.getElementById('requesterIdHidden');
                if (!hiddenInput) {
                    hiddenInput = document.createElement('input');
                    hiddenInput.type = 'hidden';
                    hiddenInput.id = 'requesterIdHidden';
                    hiddenInput.name = 'requesterIdHidden';
                    requesterSelect.parentElement.appendChild(hiddenInput);
                }
                hiddenInput.value = currentUser.id;
            }
        } catch {
            requesterSelect.replaceChildren(new Option('Không xác định được người dùng', ''));
            requesterSelect.disabled = true;
        }
    };

    // Filter devices by room
    const filterDevicesByRoom = (roomId) => {
        const select = fields.deviceId;
        select.innerHTML = '<option value="">Không chọn thiết bị</option>';

        let filtered = allDevices;
        if (roomId) {
            filtered = allDevices.filter(d => d.room && String(d.room.id) === String(roomId));
        }

        filtered.forEach(item => {
            const label = `${item.deviceCode} - ${item.deviceName}` + (item.deviceType ? ` (${item.deviceType})` : '');
            select.add(new Option(label, item.id));
        });

        // Update preview
        previewDevice.textContent = 'Không chọn';
        previewDevice.style.color = 'var(--text-muted)';
    };

    // Init form options
    const loadFormOptions = async () => {
        await initRequesterField();
        await loadOptions('/api/categories', fields.categoryId, '-- Chọn danh mục sự cố --', (item) => item.name);
        await loadOptions('/api/rooms', fields.roomId, 'Không chọn phòng', (item) => `${item.roomCode} - ${item.roomName}`);

        // Load all devices for cascading
        try {
            const dRes = await fetch('/api/devices', { headers: { Accept: 'application/json' } });
            if (dRes.ok) {
                const dData = await dRes.json();
                if (dData.success && Array.isArray(dData.data)) {
                    allDevices = dData.data;
                    filterDevicesByRoom('');
                }
            }
        } catch (e) {
            console.error('Failed to load devices', e);
        }
    };

    // Input & preview events
    fields.title.addEventListener('input', function() {
        const len = this.value.length;
        document.getElementById('titleCount').textContent = len;
        previewTitle.textContent = this.value.trim() || 'Chưa nhập...';
        previewTitle.style.color = this.value.trim() ? 'var(--text-primary)' : 'var(--text-muted)';
        if (this.value.trim()) setFieldError('title', '');
    });

    fields.description.addEventListener('input', function() {
        const len = this.value.length;
        document.getElementById('descCount').textContent = len;
        if (this.value.trim()) setFieldError('description', '');
    });

    fields.categoryId.addEventListener('change', function() {
        const sel = this.options[this.selectedIndex];
        previewCategory.textContent = sel.value ? sel.text : 'Chưa chọn';
        previewCategory.style.color = sel.value ? 'var(--text-primary)' : 'var(--text-muted)';
        if (sel.value) setFieldError('categoryId', '');
    });

    fields.roomId.addEventListener('change', function() {
        const sel = this.options[this.selectedIndex];
        previewRoom.textContent = sel.value ? sel.text : 'Không chọn';
        previewRoom.style.color = sel.value ? 'var(--text-primary)' : 'var(--text-muted)';
        filterDevicesByRoom(sel.value);
    });

    fields.deviceId.addEventListener('change', function() {
        const sel = this.options[this.selectedIndex];
        previewDevice.textContent = sel.value ? sel.text : 'Không chọn';
        previewDevice.style.color = sel.value ? 'var(--text-primary)' : 'var(--text-muted)';
    });

    // Priority Cards Selection
    const priorityCards = document.querySelectorAll('.priority-card');
    priorityCards.forEach(card => {
        card.addEventListener('click', () => {
            priorityCards.forEach(c => c.classList.remove('selected'));
            card.classList.add('selected');
            const radio = card.querySelector('input[type="radio"]');
            if (radio) {
                radio.checked = true;
                previewPriority.innerHTML = PRIORITY_BADGES[radio.value] || '';
                setFieldError('priority', '');
            }
        });
    });

    // Dropzone Events
    fileDropzone.addEventListener('click', () => attachmentInput.click());

    fileDropzone.addEventListener('dragover', (e) => {
        e.preventDefault();
        fileDropzone.classList.add('dragover');
    });

    fileDropzone.addEventListener('dragleave', () => {
        fileDropzone.classList.remove('dragover');
    });

    fileDropzone.addEventListener('drop', (e) => {
        e.preventDefault();
        fileDropzone.classList.remove('dragover');
        if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
            handleFileSelect(e.dataTransfer.files[0]);
        }
    });

    attachmentInput.addEventListener('change', (e) => {
        if (e.target.files && e.target.files.length > 0) {
            handleFileSelect(e.target.files[0]);
        }
    });

    const handleFileSelect = (file) => {
        if (!file) return;
        const maxBytes = 15 * 1024 * 1024; // 15MB
        if (file.size > maxBytes) {
            showFeedback('Dung lượng tệp vượt quá 15MB. Vui lòng chọn tệp nhỏ hơn.', 'error');
            return;
        }

        selectedFile = file;
        previewFileName.textContent = file.name;
        previewFileSize.textContent = formatBytes(file.size);
        fileDropzone.hidden = true;
        filePreviewWrap.hidden = false;

        previewFileField.hidden = false;
        previewFileVal.textContent = `${file.name} (${formatBytes(file.size)})`;
    };

    btnRemoveFile.addEventListener('click', () => {
        selectedFile = null;
        attachmentInput.value = '';
        fileDropzone.hidden = false;
        filePreviewWrap.hidden = true;
        previewFileField.hidden = true;
        previewFileVal.textContent = '—';
    });

    // Form Validation
    const validate = () => {
        clearErrors();
        let valid = true;

        if (!fields.title.value.trim()) {
            setFieldError('title', 'Vui lòng nhập tiêu đề yêu cầu.');
            valid = false;
        } else if (fields.title.value.trim().length < 5) {
            setFieldError('title', 'Tiêu đề phải có ít nhất 5 ký tự.');
            valid = false;
        }

        if (!fields.description.value.trim()) {
            setFieldError('description', 'Vui lòng nhập mô tả chi tiết sự cố.');
            valid = false;
        } else if (fields.description.value.trim().length < 10) {
            setFieldError('description', 'Mô tả cần ít nhất 10 ký tự để kỹ thuật viên hiểu rõ sự cố.');
            valid = false;
        }

        if (!fields.categoryId.value) {
            setFieldError('categoryId', 'Vui lòng chọn danh mục sự cố.');
            valid = false;
        }

        const requesterVal = fields.requesterId.disabled
            ? document.getElementById('requesterIdHidden')?.value
            : fields.requesterId.value;

        if (!requesterVal) {
            setFieldError('requesterId', 'Không xác định được người yêu cầu.');
            valid = false;
        }

        const priorityRadio = document.querySelector('input[name="priority"]:checked');
        if (!priorityRadio) {
            setFieldError('priority', 'Vui lòng chọn mức độ ưu tiên.');
            valid = false;
        }

        return valid;
    };

    // Form Submit
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        feedback.hidden = true;
        if (!validate()) return;

        const priority = document.querySelector('input[name="priority"]:checked').value;
        const hiddenRequester = document.getElementById('requesterIdHidden');
        const requesterIdValue = (fields.requesterId.disabled && hiddenRequester)
            ? hiddenRequester.value
            : fields.requesterId.value;

        const body = {
            title: fields.title.value.trim(),
            description: fields.description.value.trim(),
            categoryId: Number(fields.categoryId.value),
            requesterId: Number(requesterIdValue),
            priority
        };
        if (fields.roomId.value) body.roomId = Number(fields.roomId.value);
        if (fields.deviceId.value) body.deviceId = Number(fields.deviceId.value);

        submitButton.disabled = true;
        const originalBtnHtml = submitButton.innerHTML;
        submitButton.innerHTML = '<div class="spinner spinner-sm" style="display:inline-block;vertical-align:middle;margin-right:6px"></div> Đang gửi yêu cầu...';

        try {
            const response = await fetch('/api/requests', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
                body: JSON.stringify(body)
            });
            const payload = await response.json();
            if (!response.ok || payload.success !== true) {
                const fieldErrors = payload.errors || {};
                Object.entries(fieldErrors).forEach(([name, msg]) => setFieldError(name, msg));
                throw new Error(payload.message || 'Tạo yêu cầu không thành công. Vui lòng kiểm tra lại.');
            }

            const createdReq = payload.data;
            const reqCode = createdReq?.requestCode || `REQ-${createdReq.id}`;

            // If file was attached, upload it
            if (selectedFile && createdReq?.id) {
                submitButton.innerHTML = '<div class="spinner spinner-sm" style="display:inline-block;vertical-align:middle;margin-right:6px"></div> Đang tải tệp đính kèm...';
                const formData = new FormData();
                formData.append('file', selectedFile);

                try {
                    await fetch(`/api/requests/${createdReq.id}/attachments`, {
                        method: 'POST',
                        body: formData
                    });
                } catch (attachErr) {
                    console.warn('File upload warning:', attachErr);
                }
            }

            if (window.showToast) {
                window.showToast('success', 'Thành công', `Yêu cầu ${reqCode} đã được tạo thành công!`);
            }
            showFeedback(`Tạo yêu cầu hỗ trợ thành công (${reqCode}). Đang chuyển hướng...`, 'success');

            setTimeout(() => {
                window.location.href = `/requests/${createdReq.id}`;
            }, 1000);

        } catch (error) {
            showFeedback(error.message || 'Không thể gửi yêu cầu hỗ trợ. Vui lòng thử lại sau.', 'error');
            submitButton.disabled = false;
            submitButton.innerHTML = originalBtnHtml;
        }
    });

    loadFormOptions();
});
