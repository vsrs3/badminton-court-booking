
/* TERMINATE */
function toggleStaff() {
    const msg = SD.isActive ? 'Bạn có chắc chắn muốn xóa nhân viên này?' 
                         : 'Bạn có chắc chắn muốn khôi phục nhân viên này?';
    if (confirm(msg)) {
        window.location.href = SD.contextPath + '/owner/staffs/toggle/' +  SD.accountId + '?redirect=detail';
    }
}