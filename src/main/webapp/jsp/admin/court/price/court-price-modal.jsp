<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="modal fade" id="priceModal" tabindex="-1">
    <div class="modal-dialog modal-md modal-dialog-centered">
        <div class="modal-content">

            <form method="POST"
                  action="${pageContext.request.contextPath}/admin/court/price/save">

                <%-- HEADER --%>
                <div class="modal-header">
                    <h5 class="modal-title">
                        <c:choose>
                            <c:when test="${not empty price}">
                                Edit Price Range
                            </c:when>
                            <c:otherwise>
                                Add Price Range
                            </c:otherwise>
                        </c:choose>
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>

                <%-- BODY --%>
                <div class="modal-body">

                    <%-- REQUIRED IDS --%>
                    <input type="hidden" name="courtId" value="${court.courtId}">
                    <c:if test="${not empty price}">
                        <input type="hidden" name="priceId" value="${price.priceId}">
                    </c:if>

                    <%-- ALERT ERROR --%>
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger">
                            <i class="bi bi-exclamation-circle"></i> ${error}
                        </div>
                    </c:if>

                    <%-- TIME RANGE --%>
                    <div class="row mb-3">
                        <div class="col">
                            <label class="form-label">Start Time</label>
                            <input type="time"
                                   name="startTime"
                                   class="form-control"
                                   required
                                   value="${price.startTime}">
                        </div>
                        <div class="col">
                            <label class="form-label">End Time</label>
                            <input type="time"
                                   name="endTime"
                                   class="form-control"
                                   required
                                   value="${price.endTime}">
                        </div>
                    </div>

                    <%-- PRICE --%>
                    <div class="mb-3">
                        <label class="form-label">Price / Hour (VND)</label>
                        <input type="number"
                               name="pricePerHour"
                               class="form-control"
                               min="0"
                               step="1000"
                               required
                               value="${price.pricePerHour}">
                    </div>

                    <small class="text-muted">
                        Time range must not overlap with existing prices.
                    </small>
                </div>

                <%-- FOOTER --%>
                <div class="modal-footer">
                    <button type="button"
                            class="btn btn-outline-secondary"
                            data-bs-dismiss="modal">
                        Cancel
                    </button>
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-check-circle"></i>
                        Save
                    </button>
                </div>

            </form>

        </div>
    </div>
</div>
