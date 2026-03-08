<!-- review-creation.jsp -->
         
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer/customer-review-adding.css" />

<style>
    /* ── CSS-only star rating ── */
    .stars-row {
        display: flex;
        flex-direction: row-reverse;
        justify-content: flex-end;
        gap: 4px;
        margin-top: 8px;
    }
    .stars-row input[type="radio"] {
        display: none;
    }
    .stars-row label {
        font-size: 2rem;
        color: #ccc;
        cursor: pointer;
        transition: color 0.15s;
        user-select: none;
    }
    /* Hover: tô sao đang hover và tất cả sao sau nó (flex-reverse trick) */
    .stars-row label:hover,
    .stars-row label:hover ~ label {
        color: #f59e0b;
    }
    /* Đã chọn: tô sao được chọn và tất cả sao sau nó */
    .stars-row input[type="radio"]:checked ~ label {
        color: #f59e0b;
    }
</style>

<div class="review-page">

    <!-- HEADER -->
    <div class="review-header">
        <a href="${pageContext.request.contextPath}/my-bookings" class="btn-back-home">
            <i data-lucide="arrow-left" class="icon-sm"></i>
            <span>Quay Lại</span>
        </a>
        <div class="review-title-group">
            <h1 class="review-page-title">Đánh giá &amp; Phản hồi</h1>
            <i data-lucide="star" class="icon-md review-title-icon"></i>
        </div>
    </div>

    <!-- REVIEW CONTAINER -->
    <div class="review-container">

        <!-- REVIEW FORM CARD -->
        <div class="review-card">
            <form id="reviewForm" method="POST"
                  action="${pageContext.request.contextPath}/reviews">

                <input type="hidden" name="action"    value="add" />
                <input type="hidden" name="bookingId" value="${sessionScope.bookingId}" />

                <div class="review-content">

                    <!-- Star Rating: radio inputs + CSS thuần, KHÔNG cần JS -->
                    <div class="review-section">
                        <span class="review-section-label">
                            <i data-lucide="star" class="icon-xs"></i>
                            Đánh giá của bạn <span style="color:red">*</span>
                        </span>
                        <!-- flex-direction: row-reverse → đặt input theo thứ tự 5 → 1 -->
                        <div class="stars-row">
                            <input type="radio" id="star5" name="rating" value="5" />
                            <label for="star5" title="Xuất sắc!">&#9733;</label>
                            <input type="radio" id="star4" name="rating" value="4" />
                            <label for="star4" title="Tốt">&#9733;</label>
                            <input type="radio" id="star3" name="rating" value="3" />
                            <label for="star3" title="Bình thường">&#9733;</label>
                            <input type="radio" id="star2" name="rating" value="2" />
                            <label for="star2" title="Tệ">&#9733;</label>
                            <input type="radio" id="star1" name="rating" value="1" />
                            <label for="star1" title="Rất tệ">&#9733;</label>
                        </div>
                        <p class="star-hint" id="starHint">Nhấn để chọn số sao</p>
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
                                      placeholder="Chia sẻ trải nghiệm của bạn về sân (không vượt quá 500 kí tự)..."></textarea>
                        </div>
                        <p class="error-msg" id="commentError" style="display:none">
                            Nhận xét cần ít nhất 5 ký tự (hoặc để trống).
                        </p>
                    </div>

                </div>

                <div class="review-actions">
                    <button type="submit" class="btn-submit-review">
                        <i data-lucide="send" class="icon-sm"></i>
                        <span>Gửi đánh giá</span>
                    </button>
                    <button type="button" class="btn-reset-review">
                        <i data-lucide="refresh-cw" class="icon-sm"></i>
                        <span>Đặt lại</span>
                    </button>
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
        <c:if test="${not empty sessionScope.facilityReview}">
            <div class="field-info-card">
                <div class="facility-body">
                    <div class="facility-section">
                        <h2 class="facility-name">
                            <c:out value="${sessionScope.facilityReview.name}"/>
                        </h2>
                        <div class="facility-address">
                            <i data-lucide="map-pin" class="icon-sm"></i>
                            <span>
                                <c:out value="${sessionScope.facilityReview.address}"/>,
                                <c:out value="${sessionScope.facilityReview.ward}"/>,
                                <c:out value="${sessionScope.facilityReview.district}"/>,
                                <c:out value="${sessionScope.facilityReview.province}"/>
                            </span>
                        </div>
                    </div>

                    <c:if test="${not empty sessionScope.facilityReview.description}">
                        <div class="facility-section">
                            <span class="review-section-label">
                                <i data-lucide="info" class="icon-xs"></i>
                                Mô tả sân
                            </span>
                            <p class="facility-desc">
                                <c:out value="${sessionScope.facilityReview.description}"/>
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
                                <c:out value="${sessionScope.facilityReview.openTime}"/>
                            </div>
                        </div>
                        <div class="hour-block">
                            <span class="hour-label close">
                                <i data-lucide="clock" class="icon-xs"></i>
                                Giờ đóng cửa
                            </span>
                            <div class="hour-value">
                                <c:out value="${sessionScope.facilityReview.closeTime}"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

    </div>
</div>

<!-- JS -->
<script>
    // Dùng event delegation → hoạt động dù inject qua innerHTML
    document.addEventListener('submit', function(e) {
        if (e.target && e.target.id === 'reviewForm') {
            var rating  = document.querySelector('input[name="rating"]:checked');
            var comment = document.getElementById('reviewComment');
            var errEl   = document.getElementById('commentError');
            var val     = comment ? comment.value.trim() : '';

            if (val.length > 0 && val.length < 5) {
                if (errEl) errEl.style.display = 'block';
                e.preventDefault();
                return;
            }
            if (errEl) errEl.style.display = 'none';

            if (!rating) {
                alert('Vui lòng chọn số sao trước khi gửi.');
                e.preventDefault();
                return;
            }
        }
    });

    document.addEventListener('click', function(e) {
        if (e.target && e.target.closest('[onclick*="resetReview"]')) {
            document.querySelectorAll('input[name="rating"]').forEach(function(r) {
                r.checked = false;
            });
            var comment = document.getElementById('reviewComment');
            var errEl   = document.getElementById('commentError');
            if (comment) comment.value = '';
            if (errEl)   errEl.style.display = 'none';
        }
    });
</script>