<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bcb.model.User" %>
<%@ page import="com.bcb.utils.SessionUtils" %>
<%
  User currentUser = SessionUtils.getCurrentUser(request);
  if (currentUser == null) {
    response.sendRedirect(request.getContextPath() + "/auth/mock-login");
    return;
  }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Admin Dashboard - BadmintonPro</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
  <style>
    body {
      font-family: 'Inter', sans-serif;
      background: linear-gradient(135deg, #064E3B 0%, #065F46 100%);
      min-height: 100vh;
      padding: 2rem;
    }
    .dashboard-container {
      max-width: 1200px;
      margin: 0 auto;
      background: white;
      border-radius: 1rem;
      padding: 2rem;
    }
    .dashboard-header {
      border-bottom: 2px solid #E5E7EB;
      padding-bottom: 1.5rem;
      margin-bottom: 2rem;
    }
    .badge-role {
      background-color: #FEE2E2;
      color: #991B1B;
      padding: 0.25rem 0.75rem;
      border-radius: 9999px;
      font-size: 0.75rem;
      font-weight: 900;
    }
  </style>
</head>
<body>
<div class="dashboard-container">
  <div class="dashboard-header">
    <div class="d-flex justify-content-between align-items-center">
      <div>
        <h1 class="mb-2">🎯 Admin Dashboard</h1>
        <p class="text-muted mb-0">Chào mừng, <strong><%= currentUser.getFullName() %></strong> <span class="badge-role">ADMIN</span></p>
      </div>
      <a href="${pageContext.request.contextPath}/home" class="btn btn-outline-success">
        <i class="bi bi-house"></i> Về trang chủ
      </a>
    </div>
  </div>

  <div class="alert alert-info">
    <i class="bi bi-info-circle"></i>
    <strong>Thông báo:</strong> Giao diện Admin Dashboard đang được phát triển bởi team khác.
  </div>

  <div class="row g-4 mt-3">
    <div class="col-md-4">
      <div class="card">
        <div class="card-body text-center">
          <i class="bi bi-people display-4 text-primary"></i>
          <h3 class="mt-3">Quản lý người dùng</h3>
          <p class="text-muted">Đang phát triển...</p>
        </div>
      </div>
    </div>
    <div class="col-md-4">
      <div class="card">
        <div class="card-body text-center">
          <i class="bi bi-building display-4 text-success"></i>
          <h3 class="mt-3">Quản lý sân</h3>
          <p class="text-muted">Đang phát triển...</p>
        </div>
      </div>
    </div>
    <div class="col-md-4">
      <div class="card">
        <div class="card-body text-center">
          <i class="bi bi-graph-up display-4 text-warning"></i>
          <h3 class="mt-3">Thống kê</h3>
          <p class="text-muted">Đang phát triển...</p>
        </div>
      </div>
    </div>
  </div>
</div>
</body>
</html>