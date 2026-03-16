<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>BadmintonPro - Giới thiệu tổng quan</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/landing.css">
</head>
<body class="landing-page">

<nav class="landing-nav">
    <div class="container py-3 d-flex align-items-center justify-content-between">
        <a href="${pageContext.request.contextPath}/landing" class="brand">
            <div class="header-logo">
                <svg viewBox="0 0 24 24" class="logo-icon" fill="currentColor">
                    <path d="M12,2L4.5,20.29L5.21,21L12,18L18.79,21L19.5,20.29L12,2Z" />
                </svg>
            </div>
            <div class="landing-brand-text">Badminton<span class="text-lime">Pro</span></div>
        </a>
        <div class="d-none d-lg-flex align-items-center gap-4">
            <a href="#usp" class="text-decoration-none text-muted fw-semibold">Tính năng</a>
            <a href="#slider" class="text-decoration-none text-muted fw-semibold">Sân nổi bật</a>
            <a href="#offer" class="text-decoration-none text-muted fw-semibold">Ưu đãi</a>
            <a href="#process" class="text-decoration-none text-muted fw-semibold">Quy trình</a>
            <a href="#reviews" class="text-decoration-none text-muted fw-semibold">Đánh giá</a>
        </div>
        <div class="d-flex align-items-center gap-2">
            <a href="${pageContext.request.contextPath}/auth/login" class="btn-auth btn-login text-decoration-none">Đăng nhập</a>
            <a href="${pageContext.request.contextPath}/jsp/auth/register.jsp" class="btn-auth btn-register text-decoration-none">Đăng ký</a>
        </div>
    </div>
</nav>

<section class="landing-hero">
    <div class="container hero-inner">
        <div class="row align-items-center g-5">
            <div class="col-lg-6">
                <div class="hero-badge mb-3">
                    <i class="bi bi-star-fill text-warning"></i>
                    Nền tảng đặt sân cầu lông hàng đầu
                </div>
                <h1 class="hero-title mb-3">
                    Nâng tầm trải nghiệm <span>đặt sân cầu lông</span>
                </h1>
                <p class="hero-subtitle mb-4">
                    BadmintonPro giúp bạn tìm sân, so sánh giá, đặt sân và thanh toán nhanh chóng.
                    Mọi thứ bạn cần cho trải nghiệm cầu lông chuyên nghiệp đều nằm ở đây.
                </p>
                <div class="hero-actions">
                    <a href="${pageContext.request.contextPath}/home" class="btn-landing-primary">Đặt sân ngay</a>
                    <a href="#usp" class="btn-landing-outline">Khám phá thêm</a>
                </div>
            </div>
            <div class="col-lg-6">
                <div class="hero-media">
                    <img src="${pageContext.request.contextPath}/assets/images/facilities/default-facility.jpg"
                         alt="Badminton court" class="w-100 h-100 object-fit-cover">
                </div>
            </div>
        </div>
    </div>
</section>

<section id="usp" class="py-5 py-lg-6 bg-white">
    <div class="container">
        <div class="text-center mb-5">
            <h2 class="section-title mb-3">Tại sao chọn BadmintonPro?</h2>
            <p class="section-subtitle">Giải pháp toàn diện cho người chơi và chủ sân, thiết kế tối ưu cho trải nghiệm đặt sân.</p>
        </div>
        <div class="row g-4">
            <div class="col-md-4">
                <div class="usp-card">
                    <div class="usp-icon"><i class="bi bi-lightning-charge-fill"></i></div>
                    <h5 class="fw-bold mb-2">Đặt sân nhanh chóng</h5>
                    <p class="text-muted mb-0">Chọn sân, chọn giờ và xác nhận chỉ trong vài bước đơn giản.</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="usp-card">
                    <div class="usp-icon"><i class="bi bi-geo-alt-fill"></i></div>
                    <h5 class="fw-bold mb-2">Tìm sân gần bạn</h5>
                    <p class="text-muted mb-0">Gợi ý địa điểm phù hợp dựa trên khu vực và nhu cầu của bạn.</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="usp-card">
                    <div class="usp-icon"><i class="bi bi-shield-check"></i></div>
                    <h5 class="fw-bold mb-2">Thanh toán an toàn</h5>
                    <p class="text-muted mb-0">Nhiều phương thức thanh toán linh hoạt, minh bạch và bảo mật.</p>
                </div>
            </div>
        </div>
    </div>
</section>

<section id="slider" class="py-5 py-lg-6">
    <div class="container">
        <div class="text-center mb-5">
            <h2 class="section-title mb-3">Một số sân nổi bật trên hệ thống</h2>
            <p class="section-subtitle">Hình ảnh từ các sân đang hoạt động trên BadmintonPro.</p>
        </div>
        <c:choose>
            <c:when test="${not empty landingFacilities}">
                <div id="landingCarousel" class="carousel slide landing-carousel" data-bs-ride="carousel" data-bs-interval="3500">
                    <div class="carousel-indicators">
                        <c:forEach var="facility" items="${landingFacilities}" varStatus="status">
                            <button type="button" data-bs-target="#landingCarousel" data-bs-slide-to="${status.index}"
                                    class="${status.first ? 'active' : ''}" aria-current="${status.first ? 'true' : 'false'}"
                                    aria-label="Slide ${status.index + 1}"></button>
                        </c:forEach>
                    </div>
                    <div class="carousel-inner">
                        <c:forEach var="facility" items="${landingFacilities}" varStatus="status">
                            <div class="carousel-item ${status.first ? 'active' : ''}">
                                <img src="${pageContext.request.contextPath}/${facility.imageUrl}" class="d-block w-100" alt="${facility.name}">
                                <div class="carousel-caption d-none d-md-block">
                                    <h5 class="mb-0"><c:out value="${facility.name}" /></h5>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                    <button class="carousel-control-prev" type="button" data-bs-target="#landingCarousel" data-bs-slide="prev">
                        <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                        <span class="visually-hidden">Previous</span>
                    </button>
                    <button class="carousel-control-next" type="button" data-bs-target="#landingCarousel" data-bs-slide="next">
                        <span class="carousel-control-next-icon" aria-hidden="true"></span>
                        <span class="visually-hidden">Next</span>
                    </button>
                </div>
            </c:when>
            <c:otherwise>
                <div class="text-center text-muted">Chưa có hình ảnh sân để hiển thị.</div>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<section id="offer" class="py-5 py-lg-6">
    <div class="container">
        <div class="offer-section p-4 p-lg-5">
            <div class="row align-items-center">
                <div class="col-lg-7">
                    <div class="offer-badge mb-3">Ưu đãi đặc biệt</div>
                    <h2 class="offer-title mb-3">Giảm ngay 20% cho lần đặt đầu tiên</h2>
                    <p class="mb-4 text-white-50">
                        Sử dụng mã <strong class="text-lime">PROSTART</strong> khi thanh toán để nhận ưu đãi.
                    </p>
                    <a href="${pageContext.request.contextPath}/home" class="btn-landing-accent">Nhận ưu đãi ngay</a>
                </div>
                <div class="col-lg-5 mt-4 mt-lg-0">
                    <img src="${pageContext.request.contextPath}/assets/images/facilities/default-facility.jpg"
                         alt="Offer" class="w-100 rounded-4 shadow-lg">
                </div>
            </div>
        </div>
    </div>
</section>

<section id="process" class="py-5 py-lg-6 bg-white">
    <div class="container">
        <div class="row align-items-center g-5">
            <div class="col-lg-6">
                <h2 class="section-title mb-4">Quy trình đặt sân đơn giản</h2>
                <div class="d-flex flex-column gap-4">
                    <div class="process-step">
                        <div class="process-step-number">01</div>
                        <div>
                            <div class="process-step-title">Tìm kiếm sân</div>
                            <p class="text-muted mb-0">Nhập khu vực hoặc tên sân bạn muốn trải nghiệm.</p>
                        </div>
                    </div>
                    <div class="process-step">
                        <div class="process-step-number">02</div>
                        <div>
                            <div class="process-step-title">Chọn giờ chơi</div>
                            <p class="text-muted mb-0">Xem lịch trống và chọn khung giờ phù hợp.</p>
                        </div>
                    </div>
                    <div class="process-step">
                        <div class="process-step-number">03</div>
                        <div>
                            <div class="process-step-title">Xác nhận &amp; chơi</div>
                            <p class="text-muted mb-0">Hoàn tất thanh toán và đến sân đúng giờ.</p>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-lg-6">
                <div class="hero-media">
                    <img src="${pageContext.request.contextPath}/assets/images/facilities/default-facility.jpg"
                         alt="Booking process" class="w-100 h-100 object-fit-cover">
                </div>
            </div>
        </div>
    </div>
</section>

<section id="reviews" class="py-5 py-lg-6">
    <div class="container">
        <div class="text-center mb-5">
            <h2 class="section-title mb-3">Đánh giá từ các địa điểm đã có</h2>
            <p class="section-subtitle">Một vài phản hồi mới nhất từ cộng đồng người chơi trên hệ thống.</p>
        </div>
        <c:choose>
            <c:when test="${not empty landingReviews}">
                <div class="reviews-grid">
                    <c:forEach var="review" items="${landingReviews}">
                        <div class="review-card">
                            <div class="review-stars mb-2">
                                <c:forEach var="i" begin="1" end="5">
                                    <span class="${i <= review.rating ? 'text-warning' : 'text-muted'}">★</span>
                                </c:forEach>
                            </div>
                            <p class="mb-3">
                                <c:choose>
                                    <c:when test="${not empty review.comment}">
                                        <c:out value="${review.comment}" />
                                    </c:when>
                                    <c:otherwise>
                                        Trải nghiệm rất tốt, đặt sân nhanh và tiện lợi.
                                    </c:otherwise>
                                </c:choose>
                            </p>
                            <div class="review-author"><c:out value="${review.reviewerName}" /></div>
                            <div class="review-facility">
                                <c:out value="${review.facilityName}" /> • <c:out value="${review.facilityAddress}" />
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="text-center text-muted">Chưa có đánh giá để hiển thị.</div>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<footer class="landing-footer py-5">
    <div class="container">
        <div class="row g-4">
            <div class="col-lg-5">
                <div class="d-flex align-items-center gap-2 mb-3">
                    <div class="header-logo">
                        <svg viewBox="0 0 24 24" class="logo-icon" fill="currentColor">
                            <path d="M12,2L4.5,20.29L5.21,21L12,18L18.79,21L19.5,20.29L12,2Z" />
                        </svg>
                    </div>
                    <div class="landing-brand-text text-white">Badminton<span class="text-lime">Pro</span></div>
                </div>
                <p class="mb-4">
                    Hệ thống đặt sân cầu lông chuyên nghiệp, kết nối cộng đồng thể thao trên toàn quốc.
                </p>
                <a href="${pageContext.request.contextPath}/home" class="btn-landing-primary">Đặt sân ngay</a>
            </div>
            <div class="col-lg-3">
                <div class="footer-title mb-3">Liên kết</div>
                <div class="d-flex flex-column gap-2">
                    <a href="#usp" class="footer-link">Tính năng</a>
                    <a href="#slider" class="footer-link">Sân nổi bật</a>
                    <a href="#offer" class="footer-link">Ưu đãi</a>
                    <a href="#process" class="footer-link">Quy trình</a>
                    <a href="#reviews" class="footer-link">Đánh giá</a>
                </div>
            </div>
            <div class="col-lg-4">
                <div class="footer-title mb-3">Tài khoản</div>
                <div class="d-flex flex-column gap-2">
                    <a href="${pageContext.request.contextPath}/auth/login" class="footer-link">Đăng nhập</a>
                    <a href="${pageContext.request.contextPath}/jsp/auth/register.jsp" class="footer-link">Đăng ký</a>
                </div>
            </div>
        </div>
        <div class="text-center mt-4 pt-4 border-top border-secondary">
            <small>© 2026 BadmintonPro. All rights reserved.</small>
        </div>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
