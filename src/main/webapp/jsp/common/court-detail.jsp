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
        <span id="detailRating">&#9733; 0.0 (0 &#273;&#225;nh gi&#225;)</span>
      </div>

      <!-- Floating Book Button -->
      <button id="detailBookBtn" class="detail-book-btn">
        &#272;&#7863;t l&#7883;ch
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
              <span>C&#7847;u l&#244;ng</span>
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
            <button class="detail-contact-btn">Li&#234;n h&#7879;</button>
          </div>
        </div>
      </div>

      <!-- Tabs Navigation -->
      <div class="detail-tabs">
        <button class="detail-tab active" data-detail-tab="info">Th&#244;ng tin</button>
        <button class="detail-tab" data-detail-tab="pricing">B&#7843;ng gi&#225;</button>
        <button class="detail-tab" data-detail-tab="images">H&#236;nh &#7843;nh</button>
        <button class="detail-tab" data-detail-tab="terms">&#272;i&#7873;u kho&#7843;n &amp; quy &#273;&#7883;nh</button>
        <button class="detail-tab" data-detail-tab="reviews">&#272;&#225;nh gi&#225;</button>
      </div>

      <!-- Tab Content -->
      <div class="detail-tab-content">
        <!-- Info Tab -->
        <div id="detailTabInfo" class="detail-tab-pane active">
          <div class="detail-section">
            <h3 class="detail-section-title text-warning">
              <i class="bi bi-info-circle-fill"></i> T&#7893;ng quan
            </h3>
            <div class="detail-overview">
              <p id="detailOverview">ch&#432;a c&#243;</p>
            </div>
          </div>
        </div>

        <div id="detailTabPricing" class="detail-tab-pane">
          <div id="detailPricingContent">
            <div class="detail-empty-state">ch&#432;a c&#243;</div>
          </div>
        </div>

        <div id="detailTabImages" class="detail-tab-pane">
          <div id="detailImagesContent">
            <div class="detail-empty-state">ch&#432;a c&#243;</div>
          </div>
        </div>

        <div id="detailTabTerms" class="detail-tab-pane">
          <div class="detail-coming-soon">
            <div class="coming-soon-icon">
              <i class="bi bi-info-circle"></i>
            </div>
            <p>D&#7919; li&#7879;u &#272;i&#7873;u kho&#7843;n &amp; quy &#273;&#7883;nh s&#7855;p ra m&#7855;t...</p>
          </div>
        </div>

        <div id="detailTabReviews" class="detail-tab-pane">
          <div id="detailReviewsContent">
            <div class="detail-empty-state">ch&#432;a c&#243; comment n&#224;o</div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Bottom Fade Effect -->
  <div class="detail-bottom-fade"></div>
</div>
