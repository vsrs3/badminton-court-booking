<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bcb.model.User" %>
<%@ page import="com.bcb.utils.SessionUtils" %>
<%
    User currentUser = SessionUtils.getCurrentUser(request);
    boolean isLoggedIn = currentUser != null;
%>
<header id="mainHeader" class="main-header">
    <!-- Top Header Area -->
    <div class="header-top">
        <div class="header-pattern"></div>

        <div class="header-content">
            <div class="d-flex align-items-center justify-content-between gap-4 w-100">
                <!-- Logo & Title -->
                <div class="d-flex align-items-center gap-3">
                    <div class="header-logo">
                        <svg viewBox="0 0 24 24" class="logo-icon" fill="currentColor">
                            <path d="M12,2L4.5,20.29L5.21,21L12,18L18.79,21L19.5,20.29L12,2Z" />
                        </svg>
                    </div>
                    <div class="d-flex flex-column overflow-hidden">
                        <h2 class="header-title">
                            CHỌN SÂN <span class="text-lime">CHƠI NGAY</span>
                        </h2>
                        <span class="header-subtitle d-none d-sm-block">
                            Nền tảng đặt sân cầu lông chuyên nghiệp số 1 Việt Nam
                        </span>
                    </div>
                </div>

                <!-- Auth Buttons -->
                <div class="d-flex gap-3 flex-shrink-0">
                    <% if (isLoggedIn) { %>
                    <!-- ✅ Logged In - Show User Info -->
                    <div class="user-info-container">
                        <div class="user-avatar">
                            <% if (currentUser.getAvatarPath() != null && !currentUser.getAvatarPath().isEmpty()) { %>
                            <img src="${pageContext.request.contextPath}/<%= currentUser.getAvatarPath() %>" alt="Avatar" />
                            <% } else { %>
                            <span class="avatar-initial"><%= currentUser.getFirstName().substring(0, 1).toUpperCase() %></span>
                            <% } %>
                        </div>
                        <div class="user-details">
                            <span class="user-name"><%= currentUser.getDisplayName() %></span>
                            <span class="user-role"><%= currentUser.getRole().getDisplayName() %></span>
                        </div>
                        <button class="btn-logout" id="logoutBtn" title="Đăng xuất">
                            <i class="bi bi-box-arrow-right"></i>
                        </button>
                    </div>
                    <% } else { %>
                    <!-- ✅ Guest - Show Login/Register Buttons -->
                    <button class="btn-auth btn-login" id="headerLoginBtn">Đăng nhập</button>
                    <button class="btn-auth btn-register" id="headerRegisterBtn">Đăng ký</button>
                    <% } %>
                </div>
            </div>
        </div>
    </div>

    <!-- Search & Actions Bar -->
    <div class="header-search-bar">
        <div class="search-container">

            <!-- Search Section (55%) -->
            <div class="search-section">
                <div class="search-input-wrapper">
                    <input
                            type="text"
                            id="searchInput"
                            class="search-input"
                            placeholder="Tìm kiếm theo tên sân, quận huyện hoặc thành phố..."
                    />
                </div>
                <button id="filterToggleBtn" class="btn-filter">
                    <i class="bi bi-sliders"></i>
                </button>
            </div>

            <!-- Actions Section (45%) -->
            <div class="actions-section">
                <button class="action-btn" id="mapBtn">
                    <i class="bi bi-map"></i>
                    <span class="action-text">Bản đồ sân</span>
                </button>
                <button class="action-btn" id="historyBtn">
                    <i class="bi bi-calendar-check"></i>
                    <span class="action-text">Lịch sử đặt</span>
                </button>
                <button class="action-btn" id="favoriteBtn">
                    <i class="bi bi-heart"></i>
                    <span class="action-text">Yêu thích</span>
                </button>
            </div>

        </div>
    </div>
</header>

<!-- ✅ Hidden input to pass login state to JavaScript -->
<input type="hidden" id="isLoggedIn" value="<%= isLoggedIn %>" />
<% if (isLoggedIn) { %>
<input type="hidden" id="currentUserRole" value="<%= currentUser.getRole().getCode() %>" />
<input type="hidden" id="currentUserId" value="<%= currentUser.getAccountId() %>" />
<% } %>