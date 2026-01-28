<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thông tin cá nhân - Badminton Booking</title>

    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="assets/css/Header.css">
    <link rel="stylesheet" href="assets/css/Footer.css">
    <link rel="stylesheet" href="assets/css/Profile.css">
</head>
<body>
<header><jsp:include page="/view/common/header.jsp"/></header>


<!-- Main Profile Page Wrapper -->
<div class="profile-page">
    <div class="profile-container">
        <div class="profile-wrapper">

            <!-- ========== SIDEBAR BÊN TRÁI ========== -->
            <aside class="profile-sidebar">
                <!-- User Info Card -->
                <div class="user-card">
                    <div class="user-avatar">
                        <c:choose>
                            <c:when test="${not empty sessionScope.customer.avatarPath}">
                                <img src="${pageContext.request.contextPath}/${sessionScope.customer.avatarPath}"
                                     alt="Avatar" class="avatar-img">
                            </c:when>
                            <c:otherwise>
                                <i class="fas fa-user-circle"></i>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="user-info">
                        <h3 class="user-name">${sessionScope.customer.fullName}</h3>
                        <p class="user-email">${sessionScope.customer.email}</p>
                    </div>
                    <button class="edit-icon">
                        <i class="fas fa-chevron-right"></i>
                    </button>
                </div>

                <!-- Member Badge -->
                <div class="member-badge">
                    <i class="fas fa-crown"></i>
                    <span>Hạng thành viên</span>
                    <i class="fas fa-chevron-right"></i>
                </div>

                <!-- Quick Actions -->
                <div class="quick-actions">
                    <a href="?section=history-courts" class="action-btn">
                        <i class="fas fa-calendar-check"></i>
                        <span>Lịch đã đặt</span>
                    </a>
                    <a href="?section=notifications" class="action-btn">
                        <i class="fas fa-bell"></i>
                        <span>Thông báo</span>
                    </a>
                    <a href="?section=reviews" class="action-btn">
                        <i class="fas fa-star"></i>
                        <span>Đánh giá</span>
                    </a>
                    <a href="?section=promotions" class="action-btn">
                        <i class="fas fa-gift"></i>
                        <span>Ưu đãi</span>
                    </a>
                </div>

                <!-- Main Menu -->
                <nav class="sidebar-menu">
                    <!-- NHÓM LỊCH SỬ -->
                    <h4 class="menu-title">Lịch sử</h4>

                    <a href="?section=history-courts" class="menu-item ${param.section == 'history-courts' ? 'active' : ''}">
                        <i class="fas fa-volleyball-ball"></i>
                        <span>Lịch sử đặt sân</span>
                        <i class="fas fa-chevron-right"></i>
                    </a>

                    <!-- NHÓM CÀI ĐẶT / HỆ THỐNG -->
                    <h4 class="menu-title">Cài đặt</h4>

                    <a href="?section=profile-info" class="menu-item ${empty param.section || param.section == 'profile' ? 'active' : ''}">
                        <i class="fas fa-user"></i>
                        <span>Thông tin cá nhân</span>
                        <i class="fas fa-chevron-right"></i>
                    </a>

                    <c:if test="${empty sessionScope.customer.googleId}">
                        <a href="?section=password" class="menu-item ${param.section == 'password' ? 'active' : ''}">
                            <i class="fas fa-lock"></i>
                            <span>Đổi mật khẩu</span>
                            <i class="fas fa-chevron-right"></i>
                        </a>
                    </c:if>

                    <a href="?section=favorite" class="menu-item ${param.section == 'address' ? 'active' : ''}">
                        <i class="fas fa-map-marker-alt"></i>
                        <span>Quản lý sân yêu thích</span>
                        <i class="fas fa-chevron-right"></i>
                    </a>

                    <a href="?section=notifications-settings" class="menu-item ${param.section == 'notifications-settings' ? 'active' : ''}">
                        <i class="fas fa-bell"></i>
                        <span>Cài đặt thông báo</span>
                        <i class="fas fa-chevron-right"></i>
                    </a>

                    <a href="?section=privacy" class="menu-item ${param.section == 'privacy' ? 'active' : ''}">
                        <i class="fas fa-shield-alt"></i>
                        <span>Bảo mật & Quyền riêng tư</span>
                        <i class="fas fa-chevron-right"></i>
                    </a>

                    <a href="?section=support" class="menu-item ${param.section == 'support' ? 'active' : ''}">
                        <i class="fas fa-headset"></i>
                        <span>Hỗ trợ</span>
                        <i class="fas fa-chevron-right"></i>
                    </a>
                </nav>
            </aside>

            <!-- ========== MAIN CONTENT BÊN PHẢI ========== -->
            <main class="profile-content">

                <!-- ========== LỊCH SỬ ĐẶT SÂN ========== -->
                <c:if test="${param.section == 'history-courts'}">
                    <jsp:include page="customer_booking_history.jsp"/>
                </c:if>

                <!-- ========== THÔNG TIN CÁ NHÂN ========== -->
                <c:if test="${empty param.section || param.section == 'profile-info'}">
                    <jsp:include page="customer_info.jsp"/>
                </c:if>

                <!-- ========== ĐỔI MẬT KHẨU ========== -->
                <c:if test="${param.section == 'password'}">
                    <jsp:include page="customer_password.jsp"/>
                </c:if>

                <!-- ========== QUẢN LÝ SÂN YÊU THÍCH ========== -->
                <c:if test="${param.section == 'favorite'}">
                    <jsp:include page="customer_favorite.jsp"/>
                </c:if>

            </main>
        </div>
    </div>
</div>

<!-- Footer -->
<footer><jsp:include page="/view/common/footer.jsp"/></footer>

<!-- JavaScript -->
<script>
    function previewAvatar(input) {
        if (input.files && input.files[0]) {
            const reader = new FileReader();
            reader.onload = function (e) {
                const preview = document.getElementById('avatarPreview');
                const icon = document.getElementById('avatarIcon');

                if (preview) {
                    preview.src = e.target.result;
                } else if (icon) {
                    const img = document.createElement('img');
                    img.src = e.target.result;
                    img.id = 'avatarPreview';
                    icon.parentNode.replaceChild(img, icon);
                }
            };
            reader.readAsDataURL(input.files[0]);
        }
    }

    function togglePassword(fieldId) {
        const field = document.getElementById(fieldId);
        const button = field.nextElementSibling;
        const icon = button.querySelector('i');

        if (field.type === 'password') {
            field.type = 'text';
            icon.classList.remove('fa-eye');
            icon.classList.add('fa-eye-slash');
        } else {
            field.type = 'password';
            icon.classList.remove('fa-eye-slash');
            icon.classList.add('fa-eye');
        }
    }

    function viewBookingDetail(bookingId) {
        window.location.href = 'booking?action=detail&id=' + bookingId;
    }

    function cancelBooking(bookingId) {
        if (confirm('Bạn có chắc chắn muốn hủy lịch đặt sân này?')) {
            window.location.href = 'booking?action=cancel&id=' + bookingId;
        }
    }

    function payBooking(bookingId) {
        window.location.href = 'booking?action=payment&id=' + bookingId;
    }

    function reviewBooking(bookingId) {
        window.location.href = 'review?action=create&booking_id=' + bookingId;
    }

    document.addEventListener('DOMContentLoaded', function () {
        const tabBtns = document.querySelectorAll('.tab-btn');
        const bookingCards = document.querySelectorAll('.booking-card');

        tabBtns.forEach(btn => {
            btn.addEventListener('click', function () {
                const status = this.getAttribute('data-status');
                tabBtns.forEach(b => b.classList.remove('active'));
                this.classList.add('active');

                bookingCards.forEach(card => {
                    if (status === 'all' || card.getAttribute('data-status') === status) {
                        card.style.display = 'block';
                    } else {
                        card.style.display = 'none';
                    }
                });
            });
        });

        const alerts = document.querySelectorAll('.alert');
        alerts.forEach(function (alert) {
            setTimeout(function () {
                alert.style.opacity = '0';
                setTimeout(function () {
                    alert.style.display = 'none';
                }, 300);
            }, 5000);
        });
    });
</script>
</body>
</html>