<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!-- Auth Modal Backdrop -->
<div id="authModalBackdrop" class="auth-modal-backdrop"></div>

<!-- Auth Modal -->
<div id="authModal" class="auth-modal">
  <!-- Close Button -->
  <button id="authModalCloseBtn" class="auth-modal-close">
    <i class="bi bi-x-lg"></i>
  </button>

  <!-- Modal Content -->
  <div class="auth-modal-content">
    <!-- Icon -->
    <div class="auth-icon">
      <i class="bi bi-shield-check"></i>
    </div>

    <!-- Title -->
    <h2 class="auth-title">
      YÊU CẦU <span class="text-lime">ĐĂNG NHẬP</span>
    </h2>

    <!-- Description -->
    <p class="auth-description">
      Vui lòng đăng nhập hoặc đăng ký tài khoản để sử dụng tính năng này và nhận nhiều ưu đãi hấp dẫn từ <span class="text-success fw-bold">BadmintonPro</span>.
    </p>

    <!-- Buttons -->
    <div class="auth-buttons">
      <button id="authLoginBtn" class="auth-btn auth-btn-primary">
        <i class="bi bi-box-arrow-in-right"></i>
        <span>ĐĂNG NHẬP NGAY</span>
      </button>

      <button id="authRegisterBtn" class="auth-btn auth-btn-secondary">
        <i class="bi bi-person-plus"></i>
        <span>TẠO TÀI KHOẢN MỚI</span>
      </button>
    </div>

    <!-- Footer -->
    <p class="auth-footer">
      TRẢI NGHIỆM ĐẶT SÂN CHUYÊN NGHIỆP SỐ 1
    </p>
  </div>
</div>