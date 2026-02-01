<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div class="map-view-container">

  <!-- Map Container -->
  <div id="leafletMap" class="leaflet-map"></div>

  <!-- Search Overlay on Map -->
  <div class="map-search-overlay">
    <div class="map-search-box">
      <i class="bi bi-geo-alt-fill text-success"></i>
      <input
              type="text"
              id="mapSearchInput"
              class="map-search-input"
              placeholder="Tìm kiếm khu vực..."
      />
    </div>
  </div>

  <!-- Quick Filters Overlay -->
  <div class="map-filters-overlay">
    <button class="map-filter-chip active" data-map-filter="near-me">
      Gần tôi
    </button>
    <button class="map-filter-chip" data-map-filter="high-rating">
      Đánh giá cao
    </button>
    <button class="map-filter-chip" data-map-filter="cheap">
      Giá rẻ
    </button>
    <button class="map-filter-chip" data-map-filter="new">
      Sân mới
    </button>
  </div>

</div>