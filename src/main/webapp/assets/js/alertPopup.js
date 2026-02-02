/*
    Hàm này dùng để gửi một thông báo trước khi muốn thực hiện hành động
    VD: Đăng xuất, Xóa tài khoản, Hủy booking, ....
 */

function setupConfirm(formId, title, text, icon, confirmBtnText) {
    const form = document.getElementById(formId);
    if (!form) return;

    form.addEventListener('submit', function(e) {
        e.preventDefault(); // Chặn form submit ngay lập tức

        Swal.fire({
            title: title,
            text: text,
            icon: icon,
            showCancelButton: true,
            confirmButtonColor: '#4e73df',
            cancelButtonColor: '#d33',
            confirmButtonText: confirmBtnText,
            cancelButtonText: 'Hủy',
            reverseButtons: true // Đưa nút Hủy sang trái, Xác nhận sang phải

        }).then((result) => {
            if (result.isConfirmed) {
                form.submit();
            }
        });
    });
}

// Hàm mới: dành riêng cho form cập nhật profile (có validation)
function setupProfileConfirm(formId, getValidationResult) {
    const form = document.getElementById(formId);
    if (!form) return;

    form.addEventListener('submit', function(e) {
        e.preventDefault();

        // Gọi hàm validation từ trang profile
        const isValid = getValidationResult();

        if (isValid) {
            Swal.fire({
                title: 'Xác nhận',
                text: 'Bạn có muốn lưu thay đổi không?',
                icon: 'question',
                showCancelButton: true,
                confirmButtonColor: '#10b981',
                cancelButtonColor: '#6b7280',
                confirmButtonText: 'Lưu',
                cancelButtonText: 'Hủy'
            }).then((result) => {
                if (result.isConfirmed) {
                    form.submit();
                }
            });
        } else {
            Swal.fire({
                title: 'Có lỗi!',
                text: 'Vui lòng kiểm tra lại thông tin (tên, email, số điện thoại).',
                icon: 'warning',
                confirmButtonColor: '#ef4444',
                confirmButtonText: 'Đã hiểu'
            });
        }
    });
}

document.addEventListener('DOMContentLoaded', function() {

    setupConfirm('logout', 'Đăng xuất ?',
        'Bạn có chắc muốn thoát không ?', 'question', 'Đăng xuất');

    setupConfirm('deleteAccount', 'Cảnh báo !',
        'Xóa tài khoản sẽ mất hết dữ liệu vĩnh viễn !', 'warning', 'Xóa ngay');


});