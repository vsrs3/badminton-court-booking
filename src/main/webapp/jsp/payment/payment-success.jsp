<%-- ============================================================
     payment-success.jsp — Thanh toán thành công
     Author: AnhTN
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bcb.dto.payment.PaymentStatusDTO" %>
<%
    String ctx = request.getContextPath();
    PaymentStatusDTO result = (PaymentStatusDTO) request.getAttribute("paymentResult");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Thanh toán thành công – BadmintonPro</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"/>
    <style>
        body { background: #f0fdf4; min-height: 100vh; display: flex; align-items: center; justify-content: center; }
        .success-card {
            background: #fff; border-radius: 1rem; box-shadow: 0 4px 24px rgba(0,0,0,.1);
            padding: 3rem 2rem; text-align: center; max-width: 500px; width: 100%;
        }
        .success-icon {
            width: 80px; height: 80px; border-radius: 50%;
            background: #059669; color: #fff; font-size: 2.5rem;
            display: flex; align-items: center; justify-content: center;
            margin: 0 auto 1.5rem;
        }
        .success-title { font-size: 1.5rem; font-weight: 800; color: #064E3B; margin-bottom: .5rem; }
        .success-msg { color: #64748b; font-size: .95rem; margin-bottom: 1.5rem; }
        .info-line { color: #475569; font-size: .9rem; margin-bottom: .25rem; }
        .info-line strong { color: #1e293b; }
    </style>
</head>
<body>
<div class="success-card">
    <div class="success-icon"><i class="bi bi-check-lg"></i></div>
    <div class="success-title">Thanh toán thành công!</div>
    <div class="success-msg">Booking của bạn đã được xác nhận.</div>

    <% if (result != null) { %>
    <div class="info-line">Mã giao dịch: <strong><%= result.getTransactionCode() %></strong></div>
    <% if (result.getBookingId() != null) { %>
    <div class="info-line">Mã booking: <strong>#<%= result.getBookingId() %></strong></div>
    <% } %>
    <% if (result.getAmount() != null) { %>
    <div class="info-line">Số tiền: <strong id="amtDisplay"></strong></div>
    <script>
        document.getElementById('amtDisplay').textContent =
            Number(<%= result.getAmount() %>).toLocaleString('vi-VN') + ' ₫';
    </script>
    <% } %>
    <% } %>

    <div class="mt-4">
        <a href="<%= ctx %>/home" class="btn btn-success btn-lg px-5">
            <i class="bi bi-house me-2"></i>Về trang chủ
        </a>
    </div>
</div>
</body>
</html>
