<%@ page import="com.bcb.model.Account" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
          <%
            // Check if user is logged in
            Account currentUser = (Account) session.getAttribute("account");
            if (currentUser != null && "CUSTOMER".equals(currentUser.getRole())) {
          %>
          <!-- ✅ Customer logged in state -->
          <a href="${pageContext.request.contextPath}/profile" class="customer-profile-link">
            <div class="customer-avatar">
              <%
                String avatarPath = currentUser.getAvatarPath();
                if (avatarPath != null && !avatarPath.isEmpty()) {
              %>
              <img src="${pageContext.request.contextPath}/uploads/<%= avatarPath %>" alt="Avatar">
              <% } else {
                // Generate default avatar with first letter
                String fullName = currentUser.getFullName();
                String firstLetter = fullName != null && !fullName.isEmpty() ?
                        fullName.substring(0, 1).toUpperCase() : "U";
              %>
              <div class="avatar-placeholder"><%= firstLetter %></div>
              <% } %>
            </div>
            <span class="customer-name"><%= currentUser.getFullName() %></span>
          </a>
          <% } else { %>
          <!-- ✅ Guest state -->
          <a href="${pageContext.request.contextPath}/auth/login" style="text-decoration: none" class="btn-auth btn-login">
            ĐĂNG NHẬP
          </a>
          <a href="${pageContext.request.contextPath}/auth/login" style="text-decoration: none" class="btn-auth btn-register">
            ĐĂNG KÝ
          </a>
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
        <button
                id="favoriteBtn"
                class="action-btn">
          <i class="bi bi-heart"></i>
          <span class="action-text">Yêu thích</span>
        </button>
      </div>

    </div>
  </div>
</header>