
/* TERMINATE */
function toggleStaff() {
    const msg = SD.isActive
        ? 'B?n c? ch?c ch?n mu?n x?a nh?n vi?n n?y'
        : 'B?n c? ch?c ch?n mu?n kh?i ph?c nh?n vi?n n?y';
    if (confirm(msg)) {
        window.location.href = SD.contextPath + '/owner/staffs/toggle/' + SD.accountId + '?redirect=detail';
    }
}
