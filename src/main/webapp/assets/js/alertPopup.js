/*
    Hàm này dùng để gửi một thông báo trước khi muốn thực hiện hành động
    VD: Đăng xuất, Xóa tài khoản, Hủy booking, ....
 */

/**
 * Hiển thị popup xác nhận
 */
async function showConfirm(title, text, icon = 'question', confirmBtnText = 'Xác nhận') {
    const result = await Swal.fire({
        title: title,
        text: text,
        icon: icon,
        showCancelButton: true,
        confirmButtonColor: '#10b981',
        cancelButtonColor: '#6b7280',
        confirmButtonText: confirmBtnText,
        cancelButtonText: 'Hủy',
        reverseButtons: true
    });

    return result.isConfirmed;
}

/**
 * Hiển thị popup cảnh báo (chỉ có nút Đóng
 */
async function showPopupWarning(title, text, icon = 'warning') {
    await Swal.fire({
        title: title,
        text: text,
        icon: icon,
        confirmButtonColor: '#ef4444',
        confirmButtonText: 'Đóng'
    });
}

/**
 * Hiển thị popup thành công
 */
async function showSuccess(title, text) {
    await Swal.fire({
        title: title,
        text: text,
        icon: 'success',
        confirmButtonColor: '#10b981',
        confirmButtonText: 'OK'
    });
}

/**
 * Hiển thị popup lỗi
 */
async function showError(title, text) {
    await Swal.fire({
        title: title,
        text: text,
        icon: 'error',
        confirmButtonColor: '#ef4444',
        confirmButtonText: 'Đóng'
    });
}
