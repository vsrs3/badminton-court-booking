<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>403 - Truy cập bị từ chối</title>

    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">

    <style>
        body {
            margin: 0;
            padding: 0;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, #064E3B 0%, #065F46 50%, #047857 100%);
            font-family: system-ui, -apple-system, sans-serif;
        }

        .error-container {
            text-align: center;
            padding: 3rem 2rem;
            max-width: 600px;
        }

        .error-icon {
            width: 8rem;
            height: 8rem;
            margin: 0 auto 2rem;
            background-color: rgba(239, 68, 68, 0.2);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            animation: pulse 2s infinite;
        }

        .error-icon i {
            font-size: 4rem;
            color: #EF4444;
        }

        @keyframes pulse {
            0%, 100% {
                transform: scale(1);
                opacity: 1;
            }
            50% {
                transform: scale(1.05);
                opacity: 0.8;
            }
        }

        .error-code {
            font-size: 6rem;
            font-weight: 900;
            color: #A3E635;
            margin: 0;
            line-height: 1;
            text-shadow: 0 4px 20px rgba(163, 230, 53, 0.3);
        }

        .error-title {
            font-size: 2rem;
            font-weight: 900;
            color: white;
            margin: 1rem 0;
            text-transform: uppercase;
            letter-spacing: -0.02em;
        }

        .error-message {
            font-size: 1.125rem;
            color: rgba(255, 255, 255, 0.8);
            margin: 1.5rem 0;
            line-height: 1.6;
        }

        .error-details {
            background-color: rgba(0, 0, 0, 0.2);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 0.75rem;
            padding: 1.5rem;
            margin: 2rem 0;
        }

        .error-details p {
            color: rgba(255, 255, 255, 0.7);
            font-size: 0.9375rem;
            margin: 0.5rem 0;
        }

        .error-details strong {
            color: #A3E635;
        }

        .error-actions {
            display: flex;
            gap: 1rem;
            justify-content: center;
            margin-top: 2rem;
            flex-wrap: wrap;
        }

        .btn {
            padding: 1rem 2rem;
            border-radius: 0.75rem;
            font-weight: 700;
            font-size: 0.9375rem;
            text-decoration: none;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            transition: all 0.3s;
            border: none;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
        }

        .btn-primary {
            background-color: #A3E635;
            color: #064E3B;
            box-shadow: 0 4px 14px rgba(163, 230, 53, 0.4);
        }

        .btn-primary:hover {
            background-color: #BEF264;
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(163, 230, 53, 0.5);
        }

        .btn-secondary {
            background-color: rgba(255, 255, 255, 0.1);
            color: white;
            border: 2px solid rgba(255, 255, 255, 0.3);
        }

        .btn-secondary:hover {
            background-color: rgba(255, 255, 255, 0.2);
            border-color: rgba(255, 255, 255, 0.5);
        }

        @media (max-width: 576px) {
            .error-code {
                font-size: 4rem;
            }

            .error-title {
                font-size: 1.5rem;
            }

            .error-actions {
                flex-direction: column;
            }

            .btn {
                width: 100%;
                justify-content: center;
            }
        }
    </style>
</head>
<body>

<div class="error-container">
    <div class="error-icon">
        <i class="bi bi-shield-lock-fill"></i>
    </div>

    <h1 class="error-code">403</h1>
    <h2 class="error-title">Truy cập bị từ chối</h2>

    <p class="error-message">
        Bạn không có quyền truy cập vào khu vực này.<br>
        Vui lòng kiểm tra lại quyền truy cập của bạn.
    </p>

    <div class="error-details">
        <p><strong>Lý do:</strong> Tài khoản của bạn không có quyền truy cập trang này.</p>
        <%
            com.bcb.model.Account currentUser = (com.bcb.model.Account) session.getAttribute("account");
            if (currentUser != null) {
        %>
        <p><strong>Tài khoản:</strong> <%= currentUser.getEmail() %></p>
        <p><strong>Vai trò:</strong> <%= currentUser.getRole() %></p>
        <% } else { %>
        <p><strong>Trạng thái:</strong> Chưa đăng nhập</p>
        <% } %>
    </div>

    <div class="error-actions">
        <%
            if (currentUser != null) {
                String role = currentUser.getRole();
                String dashboardUrl = "/";

                switch (role) {
                    case "ADMIN":
                        dashboardUrl = request.getContextPath() + "/admin/dashboard";
                        break;
                    case "OWNER":
                        dashboardUrl = request.getContextPath() + "/owner/dashboard";
                        break;
                    case "STAFF":
                        dashboardUrl = request.getContextPath() + "/staff/dashboard";
                        break;
                    default:
                        dashboardUrl = request.getContextPath() + "/";
                }
        %>
        <a href="<%= dashboardUrl %>" class="btn btn-primary">
            <i class="bi bi-house-door-fill"></i>
            <span>Về trang chủ</span>
        </a>
        <a href="${pageContext.request.contextPath}/auth/logout" class="btn btn-secondary">
            <i class="bi bi-box-arrow-right"></i>
            <span>Đăng xuất</span>
        </a>
        <% } else { %>
        <a href="${pageContext.request.contextPath}/auth/login" class="btn btn-primary">
            <i class="bi bi-box-arrow-in-right"></i>
            <span>Đăng nhập</span>
        </a>
        <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">
            <i class="bi bi-house-door-fill"></i>
            <span>Về trang chủ</span>
        </a>
        <% } %>
    </div>
</div>

</body>
</html>