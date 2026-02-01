<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!-- ========== CÀI ĐẶT ========== -->
<div class="profile-section">
    <div class="section-header">
        <h2>
            <i class="fas fa-cog"></i>
            Cài đặt
        </h2>
    </div>

    <!-- Success/Error Messages -->
    <c:if test="${not empty sessionScope.successMessage}">
        <div class="alert alert-success">
            <i class="fas fa-check-circle"></i>
                ${sessionScope.successMessage}
        </div>
        <c:remove var="successMessage" scope="session"/>
    </c:if>

    <c:if test="${not empty sessionScope.errorMessage}">
        <div class="alert alert-error">
            <i class="fas fa-exclamation-circle"></i>
                ${sessionScope.errorMessage}
        </div>
        <c:remove var="errorMessage" scope="session"/>
    </c:if>

    <!-- Settings List -->
    <div class="settings-list">

        <!-- ========== CÀI ĐẶT THÔNG BÁO ========== -->
        <div class="setting-card">
            <div class="setting-header clickable" onclick="toggleNotificationSettings()">
                <div class="setting-icon notification-icon">
                    <i class="fas fa-bell"></i>
                </div>
                <div class="setting-info">
                    <h3>Cài đặt thông báo</h3>
                    <p>Quản lý thông báo và cảnh báo từ hệ thống</p>
                </div>
            </div>

            <div class="setting-content" id="notification-settings" style="display: none;">
                <form action="settings?action=update-notifications" method="post" class="settings-form">
                    <div class="setting-option">
                        <div class="option-info">
                            <label for="emailNotif">
                                <i class="fas fa-envelope"></i>
                                Thông báo qua Email
                            </label>
                            <span class="option-desc">Nhận thông báo về đặt sân và ưu đãi qua email</span>
                        </div>
                        <label class="toggle-switch">
                            <input type="checkbox" id="emailNotif" name="emailNotif"
                            ${sessionScope.account.emailNotification ? 'checked' : ''}>
                            <span class="toggle-slider"></span>
                        </label>
                    </div>

                    <div class="setting-option">
                        <div class="option-info">
                            <label for="smsNotif">
                                <i class="fas fa-sms"></i>
                                Thông báo qua SMS
                            </label>
                            <span class="option-desc">Nhận tin nhắn SMS cho các lịch đặt quan trọng</span>
                        </div>
                        <label class="toggle-switch">
                            <input type="checkbox" id="smsNotif" name="smsNotif"
                            ${sessionScope.account.smsNotification ? 'checked' : ''}>
                            <span class="toggle-slider"></span>
                        </label>
                    </div>

                    <div class="setting-option">
                        <div class="option-info">
                            <label for="pushNotif">
                                <i class="fas fa-mobile-alt"></i>
                                Thông báo đẩy
                            </label>
                            <span class="option-desc">Nhận thông báo trực tiếp trên thiết bị</span>
                        </div>
                        <label class="toggle-switch">
                            <input type="checkbox" id="pushNotif" name="pushNotif"
                            ${sessionScope.account.pushNotification ? 'checked' : ''}>
                            <span class="toggle-slider"></span>
                        </label>
                    </div>

                    <div class="setting-option">
                        <div class="option-info">
                            <label for="promoNotif">
                                <i class="fas fa-gift"></i>
                                Thông báo khuyến mãi
                            </label>
                            <span class="option-desc">Nhận thông tin về các chương trình ưu đãi mới</span>
                        </div>
                        <label class="toggle-switch">
                            <input type="checkbox" id="promoNotif" name="promoNotif"
                            ${sessionScope.account.promoNotification ? 'checked' : ''}>
                            <span class="toggle-slider"></span>
                        </label>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-save"></i>
                            Lưu thay đổi
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- ========== CHUYỂN ĐỔI NGÔN NGỮ ========== -->
        <div class="setting-card">
            <div class="setting-header clickable" onclick="toggleLanguageSettings()">
                <div class="setting-icon language-icon">
                    <i class="fas fa-language"></i>
                </div>
                <div class="setting-info">
                    <h3>Ngôn ngữ</h3>
                    <p>Chọn ngôn ngữ hiển thị trên hệ thống</p>
                </div>
            </div>

            <div class="setting-content" id="language-settings" style="display: none;">
                <div class="language-options">
                    <div class="language-option ${empty sessionScope.language || sessionScope.language == 'vi' ? 'active' : ''}"
                         onclick="changeLanguage('vi')">
                        <div class="language-flag">
                            <img src="https://flagcdn.com/w40/vn.png" alt="Tiếng Việt">
                        </div>
                        <div class="language-name">
                            <strong>Tiếng Việt</strong>
                            <span>Vietnamese</span>
                        </div>
                        <c:if test="${empty sessionScope.language || sessionScope.language == 'vi'}">
                            <i class="fas fa-check-circle language-check"></i>
                        </c:if>
                    </div>

                    <div class="language-option ${sessionScope.language == 'en' ? 'active' : ''}"
                         onclick="changeLanguage('en')">
                        <div class="language-flag">
                            <img src="https://flagcdn.com/w40/us.png" alt="English">
                        </div>
                        <div class="language-name">
                            <strong>English</strong>
                            <span>Tiếng Anh</span>
                        </div>
                        <c:if test="${sessionScope.language == 'en'}">
                            <i class="fas fa-check-circle language-check"></i>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>

        <!-- ========== ĐĂNG XUẤT ========== -->
        <form id="logout" method="GET" action="customerController" class="setting-card-form" onsubmit="setupConfirm(this)">
            <input type="hidden" name="action" value="logout">
            <button type="submit" class="setting-card setting-card-button">
                <div class="setting-header">
                    <div class="setting-icon logout-icon">
                        <i class="fas fa-sign-out-alt"></i>
                    </div>
                    <div class="setting-info">
                        <h3>Đăng xuất</h3>
                        <p>Đăng xuất khỏi tài khoản của bạn</p>
                    </div>
                </div>
            </button>
        </form>

        <!-- ========== XÓA TÀI KHOẢN ========== -->
        <form id="deleteAccount" method="GET" action="customerController" class="setting-card-form" onsubmit="setupConfirm(this)">
            <input type="hidden" name="action" value="deleteAccount">
            <button type="submit" class="setting-card setting-card-button danger-card">
                <div class="setting-header">
                    <div class="setting-icon danger-icon">
                        <i class="fas fa-trash-alt"></i>
                    </div>
                    <div class="setting-info">
                        <h3>Xóa tài khoản</h3>
                        <p>Xóa vĩnh viễn tài khoản và toàn bộ dữ liệu của bạn</p>
                    </div>
                </div>
            </button>
        </form>
    </div>
</div>

<script>
    function toggleNotificationSettings() {
        const content = document.getElementById('notification-settings');

        if (content.style.display === 'none') {
            content.style.display = 'block';
        } else {
            content.style.display = 'none';
        }
    }

    function toggleLanguageSettings() {
        const content = document.getElementById('language-settings');

        if (content.style.display === 'none') {
            content.style.display = 'block';
        } else {
            content.style.display = 'none';
        }
    }
</script>
