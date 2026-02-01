<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!-- Filter Backdrop -->
<div id="filterBackdrop" class="filter-backdrop"></div>

<!-- Filter Panel Sidebar -->
<div id="filterPanel" class="filter-panel">

  <!-- Header -->
  <div class="filter-header">
    <div class="d-flex align-items-center gap-3">
      <i class="bi bi-sliders text-lime"></i>
      <h2 class="filter-title">Bộ lọc nâng cao</h2>
    </div>
    <button id="filterCloseBtn" class="filter-close-btn">
      <i class="bi bi-x-lg"></i>
    </button>
  </div>

  <!-- Content -->
  <div class="filter-content">

    <!-- Region Section -->
    <section class="filter-section">
      <h3 class="filter-section-title">
        <i class="bi bi-geo-alt-fill text-success"></i> Theo khu vực
      </h3>

      <div class="filter-group">
        <label class="filter-label">Tỉnh / Thành phố</label>
        <select id="filterProvince" class="filter-select">
          <option value="">Tất cả tỉnh thành</option>
        </select>
      </div>

      <div class="filter-group">
        <label class="filter-label">Quận / Huyện</label>
        <select id="filterDistrict" class="filter-select" disabled>
          <option value="">Tất cả quận huyện</option>
        </select>
      </div>

    <!-- Distance Section -->
    <section class="filter-section">
      <h3 class="filter-section-title">
        <i class="bi bi-navigation-fill text-success"></i> Theo khoảng cách
      </h3>

      <div class="filter-distance-grid">
        <button class="filter-distance-btn" data-distance="5">
          Dưới 5 km
        </button>
        <button class="filter-distance-btn" data-distance="10">
          Dưới 10 km
        </button>
        <button class="filter-distance-btn" data-distance="20">
          Dưới 20 km
        </button>
        <button class="filter-distance-btn" data-distance="50">
          Dưới 50 km
        </button>
      </div>
    </section>

  </div>

  <!-- Footer Actions -->
  <div class="filter-footer">
    <button id="filterResetBtn" class="btn-filter-reset">
      <i class="bi bi-arrow-clockwise"></i> Làm mới
    </button>
    <button id="filterApplyBtn" class="btn-filter-apply">
      Áp dụng bộ lọc
    </button>
  </div>

</div>

