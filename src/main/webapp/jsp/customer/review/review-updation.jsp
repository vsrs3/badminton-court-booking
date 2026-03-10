<%-- review-updation.jsp --%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer/customer-review-updation.css" />

<c:choose>
    <c:when test="${not empty sessionScope.userReview}">
        <c:set var="r" value="${sessionScope.userReview}" />
        <c:set var="f" value="${sessionScope.facilityReview}" />

        <div class="review-page">

            <!-- HEADER -->
            <div class="review-header">
                <a href="${pageContext.request.contextPath}/my-bookings">
                    <i data-lucide="arrow-left" class="icon-sm"></i>
                    <span>Quay lại </span>
                </a>
                <div class="review-title-group">
                    <h1 class="review-page-title">Chỉnh sửa đánh giá</h1>
                    <i data-lucide="pen" class="icon-md review-title-icon"></i>
                </div>
            </div>

            <!-- REVIEW CONTAINER -->
            <div class="review-container">

                <!-- REVIEW FORM CARD -->
                <div class="review-card">
                    <form id="reviewUpdateForm" method="POST" action="${pageContext.request.contextPath}/reviews">

                        <input type="hidden" name="action"    value="edit" />
                        <input type="hidden" name="bookingId" value="${r.bookingId}" />

                        <div class="review-content">

                            <!-- Star Rating -->
                            <div class="review-section">
                                <span class="review-section-label">
                                    <i data-lucide="star" class="icon-xs"></i>
                                    Đánh giá của bạn <span style="color:red">*</span>
                                </span>
                                <div class="stars-row">
                                    <input type="radio" id="star5" name="rating" value="5" ${r.rating == 5 ? 'checked' : ''} />
                                    <label for="star5" title="Xuất sắc!">&#9733;</label>
                                    <input type="radio" id="star4" name="rating" value="4" ${r.rating == 4 ? 'checked' : ''} />
                                    <label for="star4" title="Tốt">&#9733;</label>
                                    <input type="radio" id="star3" name="rating" value="3" ${r.rating == 3 ? 'checked' : ''} />
                                    <label for="star3" title="Bình thường">&#9733;</label>
                                    <input type="radio" id="star2" name="rating" value="2" ${r.rating == 2 ? 'checked' : ''} />
                                    <label for="star2" title="Tệ">&#9733;</label>
                                    <input type="radio" id="star1" name="rating" value="1" ${r.rating == 1 ? 'checked' : ''} />
                                    <label for="star1" title="Rất tệ">&#9733;</label>
                                </div>
                                <p class="star-hint" id="starHint">Nhấn để thay đổi số sao</p>
                            </div>

                            <!-- Textarea -->
                            <div class="review-section">
                                <span class="review-section-label">
                                    <i data-lucide="message-square" class="icon-xs"></i>
                                    Nhận xét <span class="review-optional">(tùy chọn)</span>
                                </span>
                                <div class="textarea-wrap">
                                    <textarea id="reviewComment" name="comment"
                                              class="review-textarea" maxlength="500"
                                              placeholder="Chia sẻ trải nghiệm của bạn về sân (không vượt quá 500 kí tự)..."><c:out value="${r.comment}"/></textarea>
                                </div>
                                <p class="error-msg" id="commentError" style="display:none">
                                    Nhận xét cần ít nhất 5 ký tự (hoặc để trống).
                                </p>
                            </div>

                        </div>

                        <div class="review-actions">
                            <button type="submit" class="btn-submit-review">
                                <i data-lucide="save" class="icon-sm"></i>
                                <span>Lưu thay đổi</span>
                            </button>
                            <a href="${pageContext.request.contextPath}/reviews?action=view&bookingId=${r.bookingId}"
                               class="btn-reset-review">
                                <i data-lucide="x" class="icon-sm"></i>
                                <span>Hủy chỉnh sửa</span>
                            </a>
                        </div>

                        <c:if test="${not empty error}">
                            <div class="review-notification error">
                                <i data-lucide="alert-circle" class="icon-sm"></i>
                                <span>${error}</span>
                            </div>
                        </c:if>

                    </form>
                </div>

                <!-- FACILITY INFO CARD -->
                <c:if test="${not empty f}">
                    <div class="field-info-card">
                        <div class="facility-body">
                            <div class="facility-section">
                                <h2 class="facility-name">
                                    <c:out value="${f.name}"/>
                                </h2>
                                <div class="facility-address">
                                    <i data-lucide="map-pin" class="icon-sm"></i>
                                    <span>
                                        <c:out value="${f.address}"/>,
                                        <c:out value="${f.ward}"/>,
                                        <c:out value="${f.district}"/>,
                                        <c:out value="${f.province}"/>
                                    </span>
                                </div>
                            </div>

                            <c:if test="${not empty f.description}">
                                <div class="facility-section">
                                    <span class="review-section-label">
                                        <i data-lucide="info" class="icon-xs"></i>
                                        Mô tả sân
                                    </span>
                                    <p class="facility-desc">
                                        <c:out value="${f.description}"/>
                                    </p>
                                </div>
                            </c:if>

                            <div class="facility-hours">
                                <div class="hour-block">
                                    <span class="hour-label open">
                                        <i data-lucide="clock" class="icon-xs"></i>
                                        Giờ mở cửa
                                    </span>
                                    <div class="hour-value">
                                        <c:out value="${f.openTime}"/>
                                    </div>
                                </div>
                                <div class="hour-block">
                                    <span class="hour-label close">
                                        <i data-lucide="clock" class="icon-xs"></i>
                                        Giờ đóng cửa
                                    </span>
                                    <div class="hour-value">
                                        <c:out value="${f.closeTime}"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:if>

            </div>
        </div>

        <script>
            function validateReviewUpdate() {
                var rating  = document.querySelector('input[name="rating"]:checked');
                var comment = document.getElementById('reviewComment');
                var errEl   = document.getElementById('commentError');
                var val     = comment ? comment.value.trim() : '';

                if (val.length > 0 && val.length < 5) {
                    if (errEl) errEl.style.display = 'block';
                    return false;
                }
                if (errEl) errEl.style.display = 'none';

                if (!rating) {
                    alert('Vui lòng chọn số sao trước khi lưu.');
                    return false;
                }
                return true;
            }
        </script>

    </c:when>
    <c:otherwise>
        <div class="p-6 text-red-500 font-semibold">Không tìm thấy đánh giá để chỉnh sửa.</div>
    </c:otherwise>
</c:choose>
