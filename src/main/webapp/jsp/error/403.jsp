<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bcb.model.User" %>
<%@ page import="com.bcb.utils.SessionUtils" %>
<%
    User currentUser = SessionUtils.getCurrentUser(request);
    String userInfo = currentUser != null ?
            currentUser.getEmail() + " (" + currentUser.getRole() + ")" :
            "Guest";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>403 - Truy cập bị từ chối</title>

    <!-- Bootstrap 5.3 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">

    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">

    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            background: linear-gradient(135deg, #064E3B 0%, #065F46 50%, #047857 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 2rem;
        }

        .error-container {
            max-width: 600px;
            width: 100%;
            background: white;
            border-radius: 1.5rem;
            padding: 3rem 2rem;
            text-align: center;
            box-shadow: 0 25px 50px rgba(0, 0, 0, 0.3);
            animation: slideIn 0.5s ease-out;
        }

        @keyframes slideIn {
            from {
                opacity: 0;
                transform: translateY(-30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .error-icon {
            width: 6rem;
            height: 6rem;
            margin: 0 auto 2rem;
            background-color: #FEE2E2;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            animation: bounce 0.8s ease-in-out;
        }

        @keyframes bounce {
            0%, 100% { transform: translateY(0); }
            50% { transform: translateY(-20px); }
        }

        .error-icon i {
            font-size: 3rem;
            color: #DC2626;
        }

        .error-code {
            font-size: 5rem;
            font-weight: 900;
            color: #064E3B;
            line-height: 1;
            margin-bottom: 1rem;
            font-style: italic;
            letter-spacing: -0.05em;
        }

        .error-title {
            font-size: 1.75rem;
            font-weight: 900;
            color: #111827;
            text-transform: uppercase;
            margin-bottom: 1rem;
            font-style: italic;
            letter-spacing: -0.02em;
        }

        .error-message {
            font-size: 1rem;
            color: #6B7280;
            line-height: 1.6;
            margin-bottom: 2rem;
        }

        .user-info {
            background-color: #F3F4F6;
            border-radius: 0.75rem;
            padding: 1rem;
            margin-bottom: 2rem;
        }

        .user-info p {
            margin: 0;
            font-size: 0.875rem;
            color: #4B5563;
        }

        .user-info strong {
            color: #064E3B;
            font-weight: 700;
        }

        .error-actions {
            display: flex;
            flex-direction: column;
            gap: 0.75rem;
        }

        .btn-action {
            width: 100%;
            padding: 1rem 1.5rem;
            border-radius: 0.75rem;
            font-size: 0.875rem;
            font-weight: 900;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            border: none;
            cursor: pointer;
            transition: all 0.2s;
            text-decoration: none;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.5rem;
        }

        .btn-primary {
            background-color: #064E3B;
            color: white;
        }

        .btn-primary:hover {
            background-color: #065F46;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(6, 78, 59, 0.3);
        }

        .btn-secondary {
            background-color: transparent;
            color: #064E3B;
            border: 2px solid #064E3B;
        }

        .btn-secondary:hover {
            background-color: #F0FDF4;
        }

        .btn-action i {
            font-size: 1.25rem;
        }

        @media (max-width: 576px) {
            .error-container {
                padding: 2rem 1.5rem;
            }

            .error-code {
                font-size: 4rem;
            }

            .error-title {
                font-size: 1.5rem;
            }
        }
    </style>
</head>
<body>
<div class="error-container">
    <div class="error-icon">
        <i class="bi bi-shield-x"></i>
    </div>

    <div class="error-code">403</div>

    <h1 class="error-title">Truy cập bị từ chối</h1>

    <p class="error-message">
        Bạn không có quyền truy cập vào trang này.
        Vui lòng liên hệ quản trị viên nếu bạn cho rằng đây là một lỗi.
    </p>

    <div class="user-info">
        <p><strong>Đang đăng nhập với:</strong> <%= userInfo %></p>
        <p><strong>URL yêu cầu:</strong> <%= request.getRequestURI() %></p>
    </div>

    <div class="error-actions">
        <a href="<%= request.getContextPath() %>/home" class="btn-action btn-primary">
            <i class="bi bi-house-door"></i>
            <span>Về trang chủ</span>
        </a>

        <button onclick="window.history.back()" class="btn-action btn-secondary">
            <i class="bi bi-arrow-left"></i>
            <span>Quay lại</span>
        </button>
    </div>
</div>
</body>
</html>