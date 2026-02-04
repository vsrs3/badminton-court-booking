<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bcb.model.Account" %>
<%
  Account admin = (Account) session.getAttribute("account");
  if (admin == null || !"ADMIN".equals(admin.getRole())) {
    response.sendRedirect(request.getContextPath() + "/auth/login");
    return;
  }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Admin Dashboard - BadmintonPro</title>

  <!-- Bootstrap 5.3 CSS -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

  <!-- Bootstrap Icons -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">

  <!-- Custom CSS -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">

  <style>
    body {
      background-color: #F9FAFB;
    }

    .dashboard-header {
      background: linear-gradient(135deg, #064E3B 0%, #065F46 100%);
      color: white;
      padding: 2rem 0;
      margin-bottom: 2rem;
    }

    .dashboard-card {
      background: white;
      border-radius: 1rem;
      padding: 2rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
      margin-bottom: 1.5rem;
    }

    .stat-card {
      background: white;
      border-radius: 1rem;
      padding: 1.5rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
      text-align: center;
    }

    .stat-icon {
      width: 3.5rem;
      height: 3.5rem;
      border-radius: 0.75rem;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 1rem;
      font-size: 1.75rem;
    }

    .stat-value {
      font-size: 2rem;
      font-weight: 900;
      color: #064E3B;
      margin: 0.5rem 0;
    }

    .stat-label {
      font-size: 0.875rem;
      color: #6B7280;
      font-weight: 600;
    }
  </style>
</head>
<body>

<!-- Dashboard Header -->
<div class="dashboard-header">
  <div class="container">
    <div class="d-flex justify-content-between align-items-center">
      <div>
        <h1 class="mb-2" style="font-weight: 900;">Admin Dashboard</h1>
        <p class="mb-0">Xin chào, <%= admin.getFullName() %>!</p>
      </div>
      <a href="${pageContext.request.contextPath}/auth/logout" class="btn btn-light">
        <i class="bi bi-box-arrow-right"></i> Đăng xuất
      </a>
    </div>
  </div>
</div>

<!-- Dashboard Content -->
<div class="container">

  <!-- Statistics Row -->
  <div class="row g-4 mb-4">
    <div class="col-md-3">
      <div class="stat-card">
        <div class="stat-icon" style="background-color: #DBEAFE; color: #1E40AF;">
          <i class="bi bi-people-fill"></i>
        </div>
        <div class="stat-value">1,234</div>
        <div class="stat-label">Người dùng</div>
      </div>
    </div>

    <div class="col-md-3">
      <div class="stat-card">
        <div class="stat-icon" style="background-color: #D1FAE5; color: #065F46;">
          <i class="bi bi-shop"></i>
        </div>
        <div class="stat-value">25</div>
        <div class="stat-label">Cơ sở</div>
      </div>
    </div>

    <div class="col-md-3">
      <div class="stat-card">
        <div class="stat-icon" style="background-color: #FEF3C7; color: #92400E;">
          <i class="bi bi-calendar-check"></i>
        </div>
        <div class="stat-value">156</div>
        <div class="stat-label">Đặt lịch hôm nay</div>
      </div>
    </div>

    <div class="col-md-3">
      <div class="stat-card">
        <div class="stat-icon" style="background-color: #E0E7FF; color: #4338CA;">
          <i class="bi bi-cash-stack"></i>
        </div>
        <div class="stat-value">45.2M</div>
        <div class="stat-label">Doanh thu (VNĐ)</div>
      </div>
    </div>
  </div>

  <!-- Quick Actions -->
  <div class="row">
    <div class="col-12">
      <div class="dashboard-card">
        <h3 style="font-weight: 900; margin-bottom: 1.5rem;">Quản lý hệ thống</h3>

        <div class="row g-3">
          <div class="col-md-4">
            <a href="#" class="btn btn-outline-success w-100 p-3">
              <i class="bi bi-people"></i> Quản lý người dùng
            </a>
          </div>
          <div class="col-md-4">
            <a href="#" class="btn btn-outline-success w-100 p-3">
              <i class="bi bi-shop"></i> Quản lý cơ sở
            </a>
          </div>
          <div class="col-md-4">
            <a href="#" class="btn btn-outline-success w-100 p-3">
              <i class="bi bi-calendar-event"></i> Xem tất cả đặt lịch
            </a>
          </div>
        </div>

        <p class="text-center text-muted mt-4 mb-0">
          <small>Các chức năng chi tiết sẽ được triển khai sau</small>
        </p>
      </div>
    </div>
  </div>

</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>