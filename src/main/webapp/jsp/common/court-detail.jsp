<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!-- Backdrop -->
<div id="courtDetailBackdrop" class="detail-backdrop"></div>

<!-- Detail Panel -->
<div id="courtDetailPanel" class="detail-panel">
  <!-- Scrollable Container -->
  <div class="detail-scroll-container">

    <!-- Top Banner -->
    <div class="detail-banner">
      <img id="detailBannerImg" src="" alt="" class="detail-banner-img" />
      <div class="detail-banner-overlay"></div>

      <!-- Navigation Buttons -->
      <div class="detail-nav-buttons">
        <button id="detailBackBtn" class="detail-nav-btn">
          <i class="bi bi-arrow-left"></i>
        </button>

        <div class="d-flex gap-2">
          <button id="detailShareBtn" class="detail-nav-btn">
            <i class="bi bi-share"></i>
          </button>
          <button id="detailFavoriteBtn" class="detail-nav-btn">
            <i class="bi bi-heart"></i>
          </button>
        </div>
      </div>

      <!-- Rating Badge -->
      <div class="detail-rating-badge">
        <span id="detailRating">★ 0.0 (0 đánh giá)</span>
      </div>

      <!-- Floating Book Button -->
      <button id="detailBookBtn" class="detail-book-btn">
        Đặt lịch
      </button>
    </div>

    <!-- Content Container -->
    <div class="detail-content">
      <!-- Main Info Card -->
      <div class="detail-info-card">
        <div class="d-flex align-items-center gap-3 mb-4">
          <div class="detail-logo">
            <img id="detailLogoImg" src="" alt="logo" />
          </div>
          <div class="flex-grow-1">
            <div class="d-flex align-items-center gap-2 mb-2">
              <i class="bi bi-check-circle-fill text-primary"></i>
              <h1 id="detailTitle" class="detail-title mb-0"></h1>
            </div>
            <div class="detail-badge">
              <div class="badge-dot"></div>
              <span>Cầu lông</span>
            </div>
          </div>
        </div>

        <div class="detail-info-list">
          <div class="info-item">
            <i class="bi bi-geo-alt-fill"></i>
            <p id="detailLocation" class="mb-0"></p>
          </div>
          <div class="info-item">
            <i class="bi bi-clock-fill"></i>
            <p id="detailOpenTime" class="mb-0"></p>
          </div>
          <div class="info-item">
            <i class="bi bi-telephone-fill"></i>
            <button class="detail-contact-btn">Liên hệ</button>
          </div>
        </div>
      </div>

      <!-- Tabs Navigation -->
      <div class="detail-tabs">
        <button class="detail-tab active" data-detail-tab="info">Thông tin</button>
        <button class="detail-tab" data-detail-tab="pricing">Bảng giá</button>
        <button class="detail-tab" data-detail-tab="images">Hình ảnh</button>
        <button class="detail-tab" data-detail-tab="terms">Điều khoản & quy định</button>
        <button class="detail-tab" data-detail-tab="reviews">Đánh giá</button>
      </div>

      <!-- Tab Content -->
      <div class="detail-tab-content">
        <!-- Info Tab -->
        <div id="detailTabInfo" class="detail-tab-pane active">
          <div class="detail-section">
            <h3 class="detail-section-title">
              <i class="bi bi-globe2"></i> Website
            </h3>
            <p class="detail-section-text text-muted fst-italic">
              Đang cập nhật...
            </p>
          </div>

          <div class="detail-section mt-4 pt-4 border-top">
            <h3 class="detail-section-title text-warning">
              <i class="bi bi-info-circle-fill"></i> Tổng quan
            </h3>
            <div class="detail-overview">
              <p id="detailOverview">
                Chào mừng bạn đến với <strong class="text-success" id="detailNameInOverview"></strong>.
                Chúng tôi tự hào cung cấp hệ thống sân tập đạt tiêu chuẩn quốc tế với đầy đủ tiện nghi,
                hệ thống chiếu sáng chống chói và mặt thảm chất lượng cao giúp giảm chấn thương.
                Môi trường tập luyện chuyên nghiệp, năng động phù hợp cho mọi lứa tuổi và trình độ.
              </p>
            </div>
          </div>
        </div>

        <!-- Other Tabs (Coming Soon) -->
        <div id="detailTabPricing" class="detail-tab-pane">
          <div class="detail-coming-soon">
            <div class="coming-soon-icon">
              <i class="bi bi-info-circle"></i>
            </div>
            <p>Dữ liệu Bảng giá sắp ra mắt...</p>
          </div>
        </div>

        <div id="detailTabImages" class="detail-tab-pane">
          <div class="detail-coming-soon">
            <div class="coming-soon-icon">
              <i class="bi bi-info-circle"></i>
            </div>
            <p>Dữ liệu Hình ảnh sắp ra mắt...</p>
          </div>
        </div>

        <div id="detailTabTerms" class="detail-tab-pane">
          <div class="detail-coming-soon">
            <div class="coming-soon-icon">
              <i class="bi bi-info-circle"></i>
            </div>
            <p>Dữ liệu Điều khoản & quy định sắp ra mắt...</p>
          </div>
        </div>

        <div id="detailTabReviews" class="detail-tab-pane">
          <div class="detail-coming-soon">
            <div class="coming-soon-icon">
              <i class="bi bi-info-circle"></i>
            </div>
            <p>Dữ liệu Đánh giá sắp ra mắt...</p>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Bottom Fade Effect -->
  <div class="detail-bottom-fade"></div>
</div>