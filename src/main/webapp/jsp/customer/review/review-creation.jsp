<%-- <!-- review-creation.jsp -->

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer/customer-review-adding.css" />

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
                  action="${pageContext.request.contextPath}/reviews"
                  onsubmit="return validateReviewForm()">

                <input type="hidden" name="action"    value="add" />
                <input type="hidden" name="bookingId" value="${sessionScope.bookingId}" />

                <div class="review-content">

                    <!-- Star Rating -->
                    <div class="review-section">
                        <span class="review-section-label">
                            <i data-lucide="star" class="icon-xs"></i>
                            Đánh giá của bạn <span style="color:red">*</span>
                        </span>
                        <div class="stars-row">
                            <input type="radio" id="star5" name="rating" value="5"
                                   onchange="document.getElementById('ratingError').style.display='none'" />
                            <label for="star5" title="Xuất sắc!">&#9733;</label>
                            <input type="radio" id="star4" name="rating" value="4"
                                   onchange="document.getElementById('ratingError').style.display='none'" />
                            <label for="star4" title="Tốt">&#9733;</label>
                            <input type="radio" id="star3" name="rating" value="3"
                                   onchange="document.getElementById('ratingError').style.display='none'" />
                            <label for="star3" title="Bình thường">&#9733;</label>
                            <input type="radio" id="star2" name="rating" value="2"
                                   onchange="document.getElementById('ratingError').style.display='none'" />
                            <label for="star2" title="Tệ">&#9733;</label>
                            <input type="radio" id="star1" name="rating" value="1"
                                   onchange="document.getElementById('ratingError').style.display='none'" />
                            <label for="star1" title="Rất tệ">&#9733;</label>
                        </div>
                        <p class="star-hint" id="starHint">Nhấn để chọn số sao</p>
                        <p class="error-msg" id="ratingError" style="display:none">
                            Vui lòng chọn số sao trước khi gửi.
                        </p>
                    </div>

                    <!-- Textarea -->
                    <div class="review-section">
                        <span class="review-section-label">
                            <i data-lucide="message-square" class="icon-xs"></i>
                            Nhận xét <span class="review-optional">(tùy chọn)</span>
                        </span>
                        <div class="textarea-wrap">
                            <textarea id="reviewComment" name="comment"
                                      class="review-textarea"
                                      placeholder="Chia sẻ trải nghiệm của bạn về sân (không vượt quá 500 kí tự)..."
                                      oninput="
                                        var len = this.value.length;
                                        var el = document.getElementById('charCount');
                                        if (el) {
                                            el.textContent = len;
                                            el.style.color = len > 500 ? '#EF4444' : (len > 450 ? '#F59E0B' : '#6B7280');
                                        }
                                        this.dataset.invalid = len > 500 ? 'true' : 'false';
                                      "></textarea>
                        </div>
                        <div class="char-counter-row">
                            <p class="error-msg" id="commentError" style="display:none">
                                Nhận xét cần ít nhất 5 ký tự (hoặc để trống).
                            </p>
                            <span class="char-counter"><span id="charCount">0</span>/500</span>
                        </div>
                    </div>

                </div>

                <div class="review-actions">
                    <button type="submit" class="btn-submit-review">
                        <i data-lucide="send" class="icon-sm"></i>
                        <span>Gửi đánh giá</span>
                    </button>
                    <button type="button" class="btn-reset-review" onclick="resetReviewForm()">
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

<script>
    function validateReviewForm() {
        var rating  = document.querySelector('input[name="rating"]:checked');
        var comment = document.getElementById('reviewComment');
        var errEl   = document.getElementById('commentError');
        var ratingErr = document.getElementById('ratingError');
        var val     = comment ? comment.value.trim() : '';

        // Kiểm tra vượt 500 ký tự
        if (comment && comment.dataset.invalid === 'true') {
            alert('Nhận xét không được vượt quá 500 ký tự. Hiện tại: ' + comment.value.length + ' ký tự.');
            comment.focus();
            return false;
        }

        // Kiểm tra tối thiểu 5 ký tự
        if (val.length > 0 && val.length < 5) {
            if (errEl) errEl.style.display = 'block';
            return false;
        }
        if (errEl) errEl.style.display = 'none';

        // Kiểm tra đã chọn sao — hiện inline thay vì alert
        if (!rating) {
            if (ratingErr) {
                ratingErr.style.display = 'block';
                ratingErr.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
            return false;
        }
        if (ratingErr) ratingErr.style.display = 'none';

        return true;
    }

    function resetReviewForm() {
        document.querySelectorAll('input[name="rating"]').forEach(function(r) { r.checked = false; });
        var comment  = document.getElementById('reviewComment');
        var errEl    = document.getElementById('commentError');
        var ratingErr = document.getElementById('ratingError');
        var charCount = document.getElementById('charCount');
        if (comment)   { comment.value = ''; comment.dataset.invalid = 'false'; }
        if (errEl)     errEl.style.display = 'none';
        if (ratingErr) ratingErr.style.display = 'none';
        if (charCount) { charCount.textContent = '0'; charCount.style.color = '#6B7280'; }
    }
</script>
 --%>
 
 <!-- review-creation.jsp -->

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer/customer-review-adding.css" />

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
                  action="${pageContext.request.contextPath}/reviews"
                  onsubmit="var rating=this.querySelector('input[name=\'rating\']:checked');var comment=document.getElementById('reviewComment');var commentErr=document.getElementById('commentError');var ratingErr=document.getElementById('ratingError');var val=comment?comment.value.trim():'';if(comment&&comment.dataset.invalid==='true'){comment.focus();return false;}if(val.length>0&&val.length<5){if(commentErr)commentErr.style.display='block';return false;}if(commentErr)commentErr.style.display='none';if(!rating){if(ratingErr){ratingErr.style.display='block';ratingErr.scrollIntoView({behavior:'smooth',block:'center'});}return false;}return true;">

                <input type="hidden" name="action"    value="add" />
                <input type="hidden" name="bookingId" value="${sessionScope.bookingId}" />

                <div class="review-content">

                    <!-- Star Rating -->
                    <div class="review-section">
                        <span class="review-section-label">
                            <i data-lucide="star" class="icon-xs"></i>
                            Đánh giá của bạn <span style="color:red">*</span>
                        </span>
                        <div class="stars-row">
                            <input type="radio" id="star5" name="rating" value="5"
                                   onchange="document.getElementById('ratingError').style.display='none'" />
                            <label for="star5" title="Xuất sắc!">&#9733;</label>
                            <input type="radio" id="star4" name="rating" value="4"
                                   onchange="document.getElementById('ratingError').style.display='none'" />
                            <label for="star4" title="Tốt">&#9733;</label>
                            <input type="radio" id="star3" name="rating" value="3"
                                   onchange="document.getElementById('ratingError').style.display='none'" />
                            <label for="star3" title="Bình thường">&#9733;</label>
                            <input type="radio" id="star2" name="rating" value="2"
                                   onchange="document.getElementById('ratingError').style.display='none'" />
                            <label for="star2" title="Tệ">&#9733;</label>
                            <input type="radio" id="star1" name="rating" value="1"
                                   onchange="document.getElementById('ratingError').style.display='none'" />
                            <label for="star1" title="Rất tệ">&#9733;</label>
                        </div>
                        <p class="star-hint">Nhấn để chọn số sao</p>
                        <p class="error-msg" id="ratingError" style="display:none">
                            Vui lòng chọn số sao trước khi gửi.
                        </p>
                    </div>

                    <!-- Textarea -->
                    <div class="review-section">
                        <span class="review-section-label">
                            <i data-lucide="message-square" class="icon-xs"></i>
                            Nhận xét <span class="review-optional">(tùy chọn)</span>
                        </span>
                        <div class="textarea-wrap">
                            <textarea id="reviewComment" name="comment"
                                      class="review-textarea"
                                      placeholder="Chia sẻ trải nghiệm của bạn về sân (không vượt quá 500 kí tự)..."
                                      oninput="
                                        var len = this.value.length;
                                        var trimLen = this.value.trim().length;
                                        var el = document.getElementById('charCount');
                                        var errEl = document.getElementById('commentError');
                                        if (el) {
                                            el.textContent = len;
                                            el.style.color = len > 500 ? '#EF4444' : (len > 450 ? '#F59E0B' : '#6B7280');
                                        }
                                        this.dataset.invalid = len > 500 ? 'true' : 'false';
                                        if (errEl) errEl.style.display = (trimLen > 0 && trimLen < 5) ? 'block' : 'none';
                                      "></textarea>
                        </div>
                        <div class="char-counter-row">
                            <p class="error-msg" id="commentError" style="display:none">
                                Nhận xét cần ít nhất 5 ký tự (hoặc để trống).
                            </p>
                            <span class="char-counter"><span id="charCount">0</span>/500</span>
                        </div>
                    </div>

                </div>

                <div class="review-actions">
                    <button type="submit" class="btn-submit-review">
                        <i data-lucide="send" class="icon-sm"></i>
                        <span>Gửi đánh giá</span>
                    </button>
                    <button type="button" class="btn-reset-review"
                            onclick="
                              document.querySelectorAll('input[name=\'rating\']').forEach(function(r){ r.checked=false; });
                              var c = document.getElementById('reviewComment');
                              var cc = document.getElementById('charCount');
                              if (c)  { c.value=''; c.dataset.invalid='false'; }
                              if (cc) { cc.textContent='0'; cc.style.color='#6B7280'; }
                              document.getElementById('commentError').style.display='none';
                              document.getElementById('ratingError').style.display='none';
                            ">
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

 