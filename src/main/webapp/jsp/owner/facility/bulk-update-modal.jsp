<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="modal fade" id="bulkUpdateModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content border-0 shadow">

            <div class="modal-header bg-primary text-white">
                <h5 class="modal-title">
                    <i class="bi bi-layers me-2"></i>Bulk Update Pricing
                </h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>

            <div class="modal-body p-4">
                <form id="bulkUpdateForm">

                    <!-- Range Helper -->
                    <div class="mb-4">
                        <label class="form-label fw-bold mb-3">1. Quick Select Time Range</label>

                        <div class="row g-2 mb-3">
                            <div class="col-md-4">
                                <button type="button" class="btn btn-outline-secondary w-100 py-3" onclick="toggleRange('MORNING')">
                                    <i class="bi bi-brightness-low fs-4 d-block mb-1"></i>
                                    Morning<br><small>(05–10)</small>
                                </button>
                            </div>

                            <div class="col-md-4">
                                <button type="button" class="btn btn-outline-secondary w-100 py-3" onclick="toggleRange('AFTERNOON')">
                                    <i class="bi bi-sun fs-4 d-block mb-1"></i>
                                    Afternoon<br><small>(10–17)</small>
                                </button>
                            </div>

                            <div class="col-md-4">
                                <button type="button" class="btn btn-outline-secondary w-100 py-3" onclick="toggleRange('EVENING')">
                                    <i class="bi bi-moon-stars fs-4 d-block mb-1"></i>
                                    Evening<br><small>(17–23)</small>
                                </button>
                            </div>
                        </div>

                        <!-- Slot Selection -->
                        <div class="border rounded p-3 bg-light">
                            <div class="row g-2" id="modalSlotList">

                                <c:forEach items="${viewData.timeSlotPrices}" var="slot">
                                    <div class="col-md-4">
                                        <div class="form-check custom-checkbox">
                                            <input class="form-check-input slot-checkbox"
                                                   type="checkbox"
                                                   name="slotIds"
                                                   value="${slot.slotId}"
                                                   data-hour="${slot.startTime.hour}"
                                                   id="chkSlot${slot.slotId}">
                                            <label class="form-check-label small" for="chkSlot${slot.slotId}">
                                                ${slot.startTimeFormatted} – ${slot.endTimeFormatted}
                                            </label>
                                        </div>
                                    </div>
                                </c:forEach>

                            </div>

                            <div class="mt-2 text-end">
                                <button type="button" class="btn btn-link btn-sm p-0 text-emerald text-decoration-none fw-bold" onclick="selectAllSlots(true)">Select All</button>
                                <span class="mx-1 text-muted">|</span>
                                <button type="button" class="btn btn-link btn-sm p-0 text-muted text-decoration-none" onclick="selectAllSlots(false)">Clear</button>
                            </div>
                        </div>
                    </div>

                    <!-- Price Input -->
                    <div>
                        <label class="form-label fw-bold">2. Set Price (VND)</label>
                        <div class="input-group input-group-lg">
                            <span class="input-group-text bg-white">₫</span>
                            <input type="number"
                                   class="form-control"
                                   id="bulkPrice"
                                   name="price"
                                   min="0"
                                   step="1000"
                                   required
                                   placeholder="e.g. 150000">
                        </div>
                        <div class="form-text mt-2 italic text-muted">
                            This price will be applied to all selected slots.
                        </div>
                    </div>

                </form>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-accent px-4" id="btnApplyBulkUpdate" onclick="submitBulkUpdate()">
                    <i class="bi bi-check2-circle me-1"></i> Apply Pricing
                </button>
            </div>

        </div>
    </div>
</div>