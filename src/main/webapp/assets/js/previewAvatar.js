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