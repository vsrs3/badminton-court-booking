<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="modal fade" id="courtModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content">

            <form method="POST"
                  action="${pageContext.request.contextPath}/admin/courts/save">

                <%-- hidden để phân biệt create / edit --%>
                <input type="hidden" name="courtId" id="courtId">
                <input type="hidden" name="facilityId" value="${facility.facilityId}">

                <div class="modal-header">
                    <h5 class="modal-title" id="courtModalTitle">
                        Add Court
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>

                <div class="modal-body">

                    <%-- COURT NAME --%>
                    <div class="mb-3">
                        <label class="form-label">
                            Court Name <span class="text-danger">*</span>
                        </label>
                        <input type="text"
                               name="courtName"
                               id="courtName"
                               class="form-control"
                               required
                               placeholder="e.g. Court 1, Court A">
                    </div>

                    <%-- DESCRIPTION --%>
                    <div class="mb-3">
                        <label class="form-label">Description</label>
                        <textarea name="description"
                                  id="courtDescription"
                                  class="form-control"
                                  rows="4"
                                  placeholder="Optional description..."></textarea>
                    </div>

                </div>

                <div class="modal-footer">
                    <button type="button"
                            class="btn btn-outline-secondary"
                            data-bs-dismiss="modal">
                        Cancel
                    </button>

                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-save"></i> Save
                    </button>
                </div>

            </form>

        </div>
    </div>
</div>

<script>
    const courtModal = document.getElementById('courtModal');

    courtModal.addEventListener('show.bs.modal', function (event) {

        const trigger = event.relatedTarget.closest('button');

        const courtId = trigger.getAttribute('data-id');

        console.log('courtId =', courtId);

        const modalTitle = document.getElementById('courtModalTitle');

        if (courtId) {
            modalTitle.innerText = 'Edit Court';

            fetch(`${pageContext.request.contextPath}/admin/courts/detail/` + courtId)
                .then(res => {
                    if (!res.ok) throw new Error('Failed to load court');
                    return res.json();
                })
                .then(data => {
                    document.getElementById('courtId').value = data.courtId;
                    document.getElementById('courtName').value = data.courtName;
                    document.getElementById('courtDescription').value = data.description || '';
                })
                .catch(err => {
                    console.error(err);
                    alert('Cannot load court data');
                });

        } else {
            modalTitle.innerText = 'Add Court';

            document.getElementById('courtId').value = '';
            document.getElementById('courtName').value = '';
            document.getElementById('courtDescription').value = '';
        }
    });

</script>