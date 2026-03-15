<%-- ============================================================
     payment-failed.jsp — Thanh toán thất bại
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
    <title>Thanh toán thất bại – BadmintonPro</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"/>
    <style>
        body { background: #fef2f2; min-height: 100vh; display: flex; align-items: center; justify-content: center; }
        .fail-card {
            background: #fff; border-radius: 1rem; box-shadow: 0 4px 24px rgba(0,0,0,.1);
            padding: 3rem 2rem; text-align: center; max-width: 500px; width: 100%;
        }
        .fail-icon {
            width: 80px; height: 80px; border-radius: 50%;
            background: #dc3545; color: #fff; font-size: 2.5rem;
            display: flex; align-items: center; justify-content: center;
            margin: 0 auto 1.5rem;
        }
        .fail-title { font-size: 1.5rem; font-weight: 800; color: #991b1b; margin-bottom: .5rem; }
        .fail-msg { color: #64748b; font-size: .95rem; margin-bottom: 1.5rem; }
        .info-line { color: #475569; font-size: .9rem; margin-bottom: .25rem; }
        .info-line strong { color: #1e293b; }
    </style>
</head>
<body>
<div class="fail-card">
    <div class="fail-icon"><i class="bi bi-x-lg"></i></div>
    <div class="fail-title">Thanh toán thất bại</div>
    <div class="fail-msg">
        <% if (result != null && result.getMessage() != null) { %>
            <%= result.getMessage() %>
        <% } else { %>
            Giao dịch không thành công. Vui lòng thử lại.
        <% } %>
    </div>

    <% if (result != null && result.getTransactionCode() != null) { %>
    <div class="info-line">Mã giao dịch: <strong><%= result.getTransactionCode() %></strong></div>
    <% } %>

    <div class="mt-4 d-flex gap-2 justify-content-center flex-wrap">
        <a href="<%= ctx %>/home" class="btn btn-outline-secondary px-4">
            <i class="bi bi-house me-1"></i>Về trang chủ
        </a>
        <a href="javascript:history.back();" class="btn btn-danger px-4">
            <i class="bi bi-arrow-counterclockwise me-1"></i>Thử lại
        </a>
    </div>
</div>
</body>
</html>
