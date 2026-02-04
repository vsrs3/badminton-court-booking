// Court Modal Logic
document.addEventListener('DOMContentLoaded', function() {
    const courtModal = document.getElementById('courtModal');
    if (!courtModal) return;

    courtModal.addEventListener('show.bs.modal', function (event) {
        const trigger = event.relatedTarget.closest('button');
        const courtId = trigger.getAttribute('data-id');
        const modalTitle = document.getElementById('courtModalTitle');
        const contextPath = courtModal.getAttribute('data-context-path');

        if (courtId) {
            modalTitle.innerText = 'Chỉnh sửa sân';
            // Optimistic loading: if we already have name/type in data attributes, use them
            const name = trigger.getAttribute('data-name');
            const type = trigger.getAttribute('data-type');
            
            if (name) document.getElementById('courtName').value = name;
            
            fetch(`${contextPath}/owner/courts/detail/` + courtId)
                .then(res => {
                    if (!res.ok) throw new Error('Failed to load court');
                    return res.json();
                })
                .then(data => {
                    document.getElementById('courtId').value = data.courtId;
                    document.getElementById('courtName').value = data.courtName;
                    document.getElementById('courtTypeId').value = data.courtTypeId;
                })
                .catch(err => {
                    console.error(err);
                    // alert('Cannot load court data');
                });

        } else {
            modalTitle.innerText = 'Thêm sân mới';
            document.getElementById('courtId').value = '';
            document.getElementById('courtName').value = '';
            document.getElementById('courtTypeId').value = '';
        }
    });
});
