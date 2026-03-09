/**
 * Court Modal Logic – handles single create, bulk create, and edit modes.
 * @author AnhTN
 */
document.addEventListener('DOMContentLoaded', function () {
    const courtModal        = document.getElementById('courtModal');
    if (!courtModal) return;

    const bulkToggle        = document.getElementById('bulkToggle');
    const bulkToggleWrapper = document.getElementById('bulkToggleWrapper');
    const bulkHint          = document.getElementById('bulkHint');
    const bulkFields        = document.getElementById('bulkFields');
    const singleNameWrapper = document.getElementById('singleNameWrapper');
    const isBulkInput       = document.getElementById('isBulkInput');
    const courtNameInput    = document.getElementById('courtName');
    const courtNamePrefix   = document.getElementById('courtNamePrefix');
    const courtCount        = document.getElementById('courtCount');
    const courtForm         = document.getElementById('courtForm');

    /* ---- toggle bulk mode on/off ---- */
    function setBulkMode(on) {
        if (on) {
            bulkFields.style.display        = '';
            bulkHint.style.display          = '';
            singleNameWrapper.style.display = 'none';
            isBulkInput.value               = 'true';
            courtNameInput.removeAttribute('required');
            courtNamePrefix.setAttribute('required', 'required');
            courtCount.setAttribute('required', 'required');
        } else {
            bulkFields.style.display        = 'none';
            bulkHint.style.display          = 'none';
            singleNameWrapper.style.display = '';
            isBulkInput.value               = 'false';
            courtNameInput.setAttribute('required', 'required');
            courtNamePrefix.removeAttribute('required');
            courtCount.removeAttribute('required');
        }
    }

    /* ---- toggle switch event ---- */
    bulkToggle.addEventListener('change', function () {
        setBulkMode(this.checked);
    });

    /* ---- modal show: detect create vs edit ---- */
    courtModal.addEventListener('show.bs.modal', function (event) {
        const trigger     = event.relatedTarget ? event.relatedTarget.closest('[data-id]') : null;
        const courtId     = trigger ? trigger.getAttribute('data-id') : null;
        const modalTitle  = document.getElementById('courtModalTitle');
        const contextPath = courtModal.getAttribute('data-context-path');

        // Always reset bulk state first
        bulkToggle.checked    = false;
        courtNamePrefix.value = '';
        courtCount.value      = '';
        setBulkMode(false);

        if (courtId) {
            /* ---- EDIT MODE: hide bulk toggle section entirely ---- */
            modalTitle.innerText            = 'Chỉnh sửa sân';
            bulkToggleWrapper.style.display = 'none';

            document.getElementById('courtId').value = courtId;

            // Optimistic fill from data attributes on the trigger button
            const name        = trigger.getAttribute('data-name');
            const description = trigger.getAttribute('data-description');
            if (name)               courtNameInput.value = name;
            if (description != null) document.getElementById('courtDescription').value = description;

            // Fetch accurate data from server
            fetch(`${contextPath}/owner/courts/detail/` + courtId)
                .then(res => {
                    if (!res.ok) throw new Error('Không thể tải thông tin sân');
                    return res.json();
                })
                .then(data => {
                    document.getElementById('courtId').value          = data.courtId;
                    courtNameInput.value                              = data.courtName;
                    document.getElementById('courtTypeId').value      = data.courtTypeId;
                    document.getElementById('courtDescription').value = data.description || '';
                })
                .catch(err => console.error(err));

        } else {
            /* ---- CREATE MODE ---- */
            modalTitle.innerText            = 'Thêm sân mới';
            bulkToggleWrapper.style.display = '';
            document.getElementById('courtId').value              = '';
            courtNameInput.value                                  = '';
            document.getElementById('courtTypeId').value         = '';
            document.getElementById('courtDescription').value    = '';
        }
    });

    /* ---- frontend validation before submit ---- */
    courtForm.addEventListener('submit', function (e) {
        const isBulk = isBulkInput.value === 'true';

        if (isBulk) {
            const prefix = courtNamePrefix.value.trim();
            const count  = parseInt(courtCount.value, 10);

            if (!prefix) {
                e.preventDefault();
                courtNamePrefix.classList.add('is-invalid');
                courtNamePrefix.focus();
                return;
            }
            courtNamePrefix.classList.remove('is-invalid');

            if (!count || count < 1 || count > 20) {
                e.preventDefault();
                courtCount.classList.add('is-invalid');
                courtCount.focus();
                return;
            }
            courtCount.classList.remove('is-invalid');

        } else {
            const name = courtNameInput.value.trim();
            if (!name) {
                e.preventDefault();
                courtNameInput.classList.add('is-invalid');
                courtNameInput.focus();
                return;
            }
            courtNameInput.classList.remove('is-invalid');
        }
    });
});
