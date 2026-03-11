<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!-- Location Permission Modal -->
<div id="locationPermissionBackdrop" class="location-modal-backdrop"></div>
<div id="locationPermissionModal" class="location-modal" role="dialog" aria-modal="true" aria-labelledby="locationPermissionTitle">
  <button id="locationPermissionCloseBtn" class="location-modal-close" aria-label="đóng">
    <i class="bi bi-x-lg"></i>
  </button>

  <div class="location-modal-content">
    <div class="location-modal-icon">
      <i class="bi bi-geo-alt-fill"></i>
    </div>
    <h3 id="locationPermissionTitle" class="location-modal-title">Chia sẻ vị trí</h3>
    <p class="location-modal-desc">
      Cho phép hiển thị khoảng cách từ  bạn đến các sân gần nhất.
    </p>

    <div class="location-modal-actions">
      <button id="locationPermissionAllowBtn" class="location-modal-btn location-modal-btn-primary">
        Cho phép
      </button>
      <button id="locationPermissionDenyBtn" class="location-modal-btn location-modal-btn-secondary">
        Không, cảm ơn
      </button>
    </div>
  </div>
</div>
