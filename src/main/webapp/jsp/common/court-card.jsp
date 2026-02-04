<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!-- This file serves as a template, actual cards will be generated via JavaScript -->
<script type="text/template" id="courtCardTemplate">
  <div class="court-card" data-court-id="{courtId}">
    <!-- Image Container -->
    <div class="court-card-image">
      <img src="{imageUrl}" alt="{courtName}" class="card-img" />

      <!-- Rating Badge -->
      <div class="card-rating-badge">
        <i class="bi bi-star-fill"></i>
        <span>{rating}</span>
      </div>

      <!-- Action Buttons -->
      <div class="card-actions">
        <button class="card-action-btn btn-favorite {favoriteClass}" data-court-id="{courtId}">
          <i class="bi bi-heart{favoriteFill}"></i>
        </button>
        <button class="card-action-btn btn-navigate" data-court-id="{courtId}">
          <i class="bi bi-geo-alt-fill"></i>
        </button>
      </div>
    </div>

    <!-- Content Area -->
    <div class="court-card-content">
      <h3 class="card-title">{courtName}</h3>

      <!-- Info and Button -->
      <div class="card-footer">
        <!-- Description -->
        <div class="card-info">
          <div class="info-row">
            <i class="bi bi-geo-alt-fill text-lime"></i>
            <span class="info-text">
                            <span class="distance">({distance})</span> {location}
                        </span>
          </div>
          <div class="info-row">
            <i class="bi bi-clock-fill text-lime"></i>
            <span class="info-text">{openTime}</span>
          </div>
        </div>

        <!-- Book Button -->
        <button class="btn-book" data-court-id="{courtId}">
          Đặt ngay
        </button>
      </div>
    </div>
  </div>
</script>