<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bcb.model.Account" %>
<%
    Account staffAccount = (Account) session.getAttribute("account");
    if (staffAccount == null || !"STAFF".equals(staffAccount.getRole())) {
        response.sendRedirect(request.getContextPath() + "/auth/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Staff Dashboard - BadmintonPro</title>

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

        .action-card {
            background: white;
            border: 2px solid #E5E7EB;
            border-radius: 1rem;
            padding: 2rem;
            text-align: center;
            transition: all 0.3s;
            cursor: pointer;
            height: 100%;
        }

        .action-card:hover {
            border-color: #064E3B;
            transform: translateY(-4px);
            box-shadow: 0 10px 30px rgba(6, 78, 59, 0.15);
        }

        .action-icon {
            width: 4rem;
            height: 4rem;
            background-color: #D1FAE5;
            color: #065F46;
            border-radius: 1rem;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 1rem;
            font-size: 2rem;
        }

        .action-title {
            font-size: 1.125rem;
            font-weight: 900;
            color: #064E3B;
            margin-bottom: 0.5rem;
        }

        .action-desc {
            font-size: 0.875rem;
            color: #6B7280;
        }
    </style>
</head>
<body>

<!-- Dashboard Header -->
<div class="dashboard-header">
    <div class="container">
        <div class="d-flex justify-content-between align-items-center">
            <div>
                <h1 class="mb-2" style="font-weight: 900;">Staff Dashboard</h1>
                <p class="mb-0">Xin chào, <%= staffAccount.getFullName() %>!</p>
                <!-- TODO: Show facility name -->
                <small style="opacity: 0.8;">Cơ sở: Đang tải...</small>
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
                <div class="stat-icon" style="background-color: #FEF3C7; color: #92400E;">
                    <i class="bi bi-calendar-check"></i>
                </div>
                <div class="stat-value">24</div>
                <div class="stat-label">Lịch hôm nay</div>
            </div>
        </div>

        <div class="col-md-3">
            <div class="stat-card">
                <div class="stat-icon" style="background-color: #DBEAFE; color: #1E40AF;">
                    <i class="bi bi-clock-history"></i>
                </div>
                <div class="stat-value">8</div>
                <div class="stat-label">Chờ check-in</div>
            </div>
        </div>

        <div class="col-md-3">
            <div class="stat-card">
                <div class="stat-icon" style="background-color: #D1FAE5; color: #065F46;">
                    <i class="bi bi-play-circle"></i>
                </div>
                <div class="stat-value">12</div>
                <div class="stat-label">Đang chơi</div>
            </div>
        </div>

        <div class="col-md-3">
            <div class="stat-card">
                <div class="stat-icon" style="background-color: #FCE7F3; color: #9F1239;">
                    <i class="bi bi-trophy"></i>
                </div>
                <div class="stat-value">45</div>
                <div class="stat-label">Vợt có sẵn</div>
            </div>
        </div>
    </div>

    <!-- Quick Actions -->
    <div class="row">
        <div class="col-12 mb-4">
            <h3 style="font-weight: 900; color: #064E3B;">Công việc hàng ngày</h3>
        </div>
    </div>

    <div class="row g-4">
        <div class="col-md-4">
            <div class="action-card">
                <div class="action-icon">
                    <i class="bi bi-qr-code-scan"></i>
                </div>
                <div class="action-title">Check-in</div>
                <div class="action-desc">Quét mã đặt lịch để check-in khách</div>
            </div>
        </div>

        <div class="col-md-4">
            <div class="action-card">
                <div class="action-icon">
                    <i class="bi bi-check-circle"></i>
                </div>
                <div class="action-title">Check-out</div>
                <div class="action-desc">Kết thúc phiên chơi và tính tiền</div>
            </div>
        </div>

        <div class="col-md-4">
            <div class="action-card">
                <div class="action-icon">
                    <i class="bi bi-calendar-plus"></i>
                </div>
                <div class="action-title">Thêm Walk-in</div>
                <div class="action-desc">Đặt lịch cho khách đến trực tiếp</div>
            </div>
        </div>

        <div class="col-md-4">
            <div class="action-card">
                <div class="action-icon">
                    <i class="bi bi-list-check"></i>
                </div>
                <div class="action-title">Lịch hôm nay</div>
                <div class="action-desc">Xem tất cả booking của ngày</div>
            </div>
        </div>

        <div class="col-md-4">
            <div class="action-card">
                <div class="action-icon">
                    <i class="bi bi-box-seam"></i>
                </div>
                <div class="action-title">Quản lý Inventory</div>
                <div class="action-desc">Cho thuê vợt, giày, shuttle</div>
            </div>
        </div>

        <div class="col-md-4">
            <div class="action-card">
                <div class="action-icon">
                    <i class="bi bi-grid-3x3"></i>
                </div>
                <div class="action-title">Sơ đồ sân</div>
                <div class="action-desc">Xem trạng thái các sân</div>
            </div>
        </div>
    </div>

    <div class="dashboard-card mt-4">
        <p class="text-center text-muted mb-0">
            <small>Các chức năng chi tiết sẽ được triển khai sau</small>
        </p>
    </div>

</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>