<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!-- AI Chatbot Floating Button -->
<div id="chatbotToggle" class="chatbot-toggle" title="Trợ lý AI">
  <div class="chatbot-toggle-icon">
    <i class="bi bi-robot"></i>
  </div>
  <div class="chatbot-toggle-pulse"></div>
</div>

<!-- AI Chatbot Widget -->
<div id="chatbotWidget" class="chatbot-widget">
  <!-- Header -->
  <div class="chatbot-header">
    <div class="chatbot-header-info">
      <div class="chatbot-avatar">
        <i class="bi bi-robot"></i>
      </div>
      <div class="chatbot-header-text">
        <h4 class="chatbot-title">BadmintonPro AI</h4>
        <span class="chatbot-status">
                    <span class="chatbot-status-dot"></span>
                    Sẵn sàng hỗ trợ
                </span>
      </div>
    </div>
    <button id="chatbotClose" class="chatbot-close-btn" title="Đóng">
      <i class="bi bi-x-lg"></i>
    </button>
  </div>

  <!-- Messages Area -->
  <div id="chatbotMessages" class="chatbot-messages">
    <!-- Welcome message -->
    <div class="chatbot-msg chatbot-msg-bot">
      <div class="chatbot-msg-avatar">
        <i class="bi bi-robot"></i>
      </div>
      <div class="chatbot-msg-content">
        <div class="chatbot-msg-bubble">
          Xin chào! 👋 Mình là trợ lý AI của <strong>BadmintonPro</strong>.<br><br>
          Mình có thể giúp bạn:
          <ul class="chatbot-help-list">
            <li>🏸 Giới thiệu sân cầu lông phù hợp</li>
            <li>💰 Tư vấn giá cả, khu vực, giờ mở cửa</li>
            <li>  Tìm sân theo địa điểm, đánh giá</li>
          </ul>
          Bấm <strong>"Xem chi tiết"</strong> ở thẻ sân để xem thông tin đầy đủ và đặt sân nhé! 😊
        </div>
        <span class="chatbot-msg-time">Bây giờ</span>
      </div>
    </div>
  </div>

  <!-- Quick Actions -->
  <div id="chatbotQuickActions" class="chatbot-quick-actions">
    <button class="chatbot-quick-btn" data-msg="Giới thiệu sân giá rẻ">
      💰 Sân giá rẻ
    </button>
    <button class="chatbot-quick-btn" data-msg="Sân nào đánh giá cao nhất?">
      ⭐ Đánh giá cao
    </button>
    <button class="chatbot-quick-btn" data-msg="Giới thiệu sân ở Hà Nội">
      📍 Tìm theo vùng
    </button>
    <button class="chatbot-quick-btn" data-msg="Sân nào có nhiều sân nhất?">
      🏸 Nhiều sân
    </button>
  </div>

  <!-- Input Area -->
  <div class="chatbot-input-area">
    <div class="chatbot-input-wrapper">
      <input type="text" id="chatbotInput" class="chatbot-input"
             placeholder="Nhập câu hỏi của bạn..."
             maxlength="1000"
             autocomplete="off" />
      <button id="chatbotSend" class="chatbot-send-btn" disabled title="Gửi">
        <i class="bi bi-send-fill"></i>
      </button>
    </div>
    <div class="chatbot-input-hint">
      <span id="chatbotCharCount">0</span>/1000
    </div>
  </div>
</div>
