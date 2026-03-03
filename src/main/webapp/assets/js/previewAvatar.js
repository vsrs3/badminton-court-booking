/*
    Hàm này dùng để xem avatar ở trong khung trước khi tải ảnh lên
    VD: Trước khi upload ảnh làm avatar
 */

function previewAvatar(input) {
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function (e) {
            const preview = document.getElementById('avatarPreview');
            const icon = document.getElementById('avatarIcon');

            if (preview) {
                preview.src = e.target.result;
            } else if (icon) {
                const img = document.createElement('img');
                img.src = e.target.result;
                img.id = 'avatarPreview';
                icon.parentNode.replaceChild(img, icon);
            }
        };
        reader.readAsDataURL(input.files[0]);
    }
}

/* ══════════════════════════════════════════
   AVATAR UPLOAD MODAL STAFF
   Endpoint riêng: POST /owner/staffs/update-avatar
   multipart/form-data  body: accountId, avatar (file)
   Response JSON: { success: true, avatarPath: "uploads/staff/..." }
══════════════════════════════════════════ */

/** Mở modal — reset state upload */
function openAvatarModal() {
    SD.av.file       = null;
    SD.av.previewUrl = null;

    /* Reset preview về ảnh hiện tại */
    _syncAvatarPreview(SD.avatarPath);

    /* Reset drop zone & error */
    document.getElementById('sd-av-input').value = '';
    _setAvError('');

    /* Disable nút Lưu — chưa chọn file */
    const saveBtn = document.getElementById('sd-av-save-btn');
    saveBtn.disabled = true;
    document.getElementById('sd-av-save-txt').textContent = 'Lưu ảnh';

    document.getElementById('sd-av-modal').classList.add('open');
}

function closeAvatarModal() {
    document.getElementById('sd-av-modal').classList.remove('open');

    /* Giải phóng object URL nếu có */
    if (SD.av.previewUrl) {
        URL.revokeObjectURL(SD.av.previewUrl);
        SD.av.previewUrl = null;
    }
    SD.av.file = null;
}

/** Đồng bộ ảnh preview trong modal */
function _syncAvatarPreview(src) {
    const img      = document.getElementById('sd-av-preview-img');
    const initials = document.getElementById('sd-av-preview-initials');
    if (!img) return;

    if (src) {
        img.src = src.startsWith('blob:') ? src : `${SD.contextPath}/${src}`;
        img.classList.remove('d-none');
        img.classList.remove('has-new');
        if (initials) initials.classList.add('d-none');
    } else {
        img.classList.add('d-none');
        if (initials) initials.classList.remove('d-none');
    }
}

/** Xử lý khi user chọn file qua input hoặc drag-drop */
function handleAvatarFile(file) {
    _setAvError('');

    if (!file) return;

    /* Validate type */
    if (!file.type.match(/^image\/(jpeg|png|webp|gif)$/)) {
        _setAvError('Chỉ chấp nhận file JPG, PNG, WEBP, GIF.');
        return;
    }
    /* Validate size — 2 MB */
    if (file.size > 2 * 1024 * 1024) {
        _setAvError('Dung lượng ảnh không được vượt quá 2MB.');
        return;
    }

    /* Giải phóng URL cũ nếu có */
    if (SD.av.previewUrl) URL.revokeObjectURL(SD.av.previewUrl);

    SD.av.file       = file;
    SD.av.previewUrl = URL.createObjectURL(file);

    /* Hiện preview */
    const img = document.getElementById('sd-av-preview-img');
    img.src = SD.av.previewUrl;
    img.classList.remove('d-none');
    img.classList.add('has-new');

    const initials = document.getElementById('sd-av-preview-initials');
    if (initials) initials.classList.add('d-none');

    /* Enable nút Lưu */
    document.getElementById('sd-av-save-btn').disabled = false;
}

/* Input file onChange */
function onAvatarInputChange(event) {
    handleAvatarFile(event.target.files[0]);
}

/* Drag & Drop handlers */
function onAvatarDragOver(event) {
    event.preventDefault();
    document.getElementById('sd-drop-zone').classList.add('dragover');
}
function onAvatarDragLeave() {
    document.getElementById('sd-drop-zone').classList.remove('dragover');
}
function onAvatarDrop(event) {
    event.preventDefault();
    document.getElementById('sd-drop-zone').classList.remove('dragover');
    const file = event.dataTransfer.files[0];
    if (file) handleAvatarFile(file);
}

/** Upload lên server */
function uploadAvatar() {
    if (!SD.av.file) return;
    closeAvatarModal();
}

/** Cập nhật ảnh tròn ở top bar sau khi upload thành công */
function _updateTopBarAvatar(newPath) {
    const wrap = document.getElementById('sd-avatar-wrap');
    if (!wrap) return;

    const newSrc = `${SD.contextPath}/${newPath}`;

    /* Nếu đang hiện initials → chuyển sang img */
    const initials = wrap.querySelector('.sd-avatar-initials');
    if (initials) {
        initials.outerHTML = `<div class="sd-avatar">
            <img src="${newSrc}" alt="avatar" id="sd-avatar-img">
        </div>`;
        return;
    }

    /* Nếu đã có img → update src */
    const img = document.getElementById('sd-avatar-img');
    if (img) img.src = newSrc;
}

/** Hiển thị / ẩn thông báo lỗi trong modal avatar */
function _setAvError(msg) {
    const el = document.getElementById('sd-av-error');
    if (!el) return;
    if (msg) {
        el.querySelector('span').textContent = msg;
        el.classList.add('show');
    } else {
        el.classList.remove('show');
    }
}