<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mock Login - BadmintonPro</title>

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
            background: linear-gradient(135deg, #064E3B 0%, #065F46 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 2rem;
        }

        .login-container {
            max-width: 480px;
            width: 100%;
            background: white;
            border-radius: 1.5rem;
            padding: 3rem 2rem;
            box-shadow: 0 25px 50px rgba(0, 0, 0, 0.3);
        }

        .login-header {
            text-align: center;
            margin-bottom: 2rem;
        }

        .login-icon {
            width: 5rem;
            height: 5rem;
            margin: 0 auto 1.5rem;
            background-color: #A3E635;
            border-radius: 1.5rem;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .login-icon i {
            font-size: 2.5rem;
            color: #064E3B;
        }

        .login-title {
            font-size: 1.75rem;
            font-weight: 900;
            color: #064E3B;
            text-transform: uppercase;
            font-style: italic;
            letter-spacing: -0.02em;
            margin-bottom: 0.5rem;
        }

        .login-subtitle {
            font-size: 0.875rem;
            color: #6B7280;
            font-weight: 600;
        }

        .mock-badge {
            display: inline-block;
            background-color: #FEF3C7;
            color: #92400E;
            padding: 0.25rem 0.75rem;
            border-radius: 9999px;
            font-size: 0.75rem;
            font-weight: 900;
            text-transform: uppercase;
            margin-top: 0.5rem;
        }

        .form-label {
            font-weight: 700;
            color: #374151;
            font-size: 0.875rem;
            margin-bottom: 0.5rem;
        }

        .form-control {
            padding: 0.75rem 1rem;
            border-radius: 0.75rem;
            border: 2px solid #E5E7EB;
            font-weight: 500;
        }

        .form-control:focus {
            border-color: #064E3B;
            box-shadow: 0 0 0 3px rgba(6, 78, 59, 0.1);
        }

        .btn-login {
            width: 100%;
            padding: 1rem;
            background-color: #064E3B;
            color: white;
            border: none;
            border-radius: 0.75rem;
            font-weight: 900;
            font-size: 0.875rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            cursor: pointer;
            transition: all 0.2s;
            margin-top: 1.5rem;
        }

        .btn-login:hover {
            background-color: #065F46;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(6, 78, 59, 0.3);
        }

        .alert {
            margin-bottom: 1.5rem;
            border-radius: 0.75rem;
            padding: 1rem;
        }

        .test-accounts {
            margin-top: 2rem;
            padding-top: 2rem;
            border-top: 1px solid #E5E7EB;
        }

        .test-accounts h3 {
            font-size: 0.875rem;
            font-weight: 900;
            text-transform: uppercase;
            color: #6B7280;
            margin-bottom: 1rem;
        }

        .account-card {
            background-color: #F9FAFB;
            border-radius: 0.75rem;
            padding: 0.75rem 1rem;
            margin-bottom: 0.5rem;
            cursor: pointer;
            transition: all 0.2s;
            border: 2px solid transparent;
        }

        .account-card:hover {
            background-color: #F0FDF4;
            border-color: #064E3B;
        }

        .account-card strong {
            color: #064E3B;
            display: block;
            font-size: 0.875rem;
        }

        .account-card small {
            color: #6B7280;
            font-size: 0.75rem;
        }

        .role-badge {
            display: inline-block;
            padding: 0.125rem 0.5rem;
            border-radius: 9999px;
            font-size: 0.625rem;
            font-weight: 900;
            text-transform: uppercase;
            margin-left: 0.5rem;
        }

        .role-customer { background-color: #DBEAFE; color: #1E40AF; }
        .role-staff { background-color: #E0E7FF; color: #4338CA; }
        .role-owner { background-color: #FCE7F3; color: #BE185D; }
        .role-admin { background-color: #FEE2E2; color: #991B1B; }
    </style>
</head>
<body>
<div class="login-container">
    <div class="login-header">
        <div class="login-icon">
            <i class="bi bi-shield-check"></i>
        </div>
        <h1 class="login-title">BadmintonPro</h1>
        <p class="login-subtitle">Mock Authentication System</p>
        <span class="mock-badge">🧪 Development Mode</span>
    </div>

    <% if (request.getAttribute("error") != null) { %>
    <div class="alert alert-danger">
        <i class="bi bi-exclamation-triangle"></i>
        <%= request.getAttribute("error") %>
    </div>
    <% } %>

    <form method="POST" action="${pageContext.request.contextPath}/auth/mock-login">
        <div class="mb-3">
            <label class="form-label">Email</label>
            <input
                    type="email"
                    name="email"
                    class="form-control"
                    placeholder="customer@test.com"
                    value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>"
                    required
            />
        </div>

        <div class="mb-3">
            <label class="form-label">Mật khẩu</label>
            <input
                    type="password"
                    name="password"
                    class="form-control"
                    placeholder="password123"
                    required
            />
        </div>

        <button type="submit" class="btn-login">
            <i class="bi bi-box-arrow-in-right"></i>
            Đăng nhập
        </button>
    </form>

    <div class="test-accounts">
        <h3>Tài khoản thử nghiệm</h3>

        <div class="account-card" onclick="fillLogin('customer@test.com', 'password123')">
            <strong>
                Customer Account
                <span class="role-badge role-customer">CUSTOMER</span>
            </strong>
            <small>customer@test.com / password123</small>
        </div>

        <div class="account-card" onclick="fillLogin('staff@test.com', 'password123')">
            <strong>
                Staff Account
                <span class="role-badge role-staff">STAFF</span>
            </strong>
            <small>staff@test.com / password123</small>
        </div>

        <div class="account-card" onclick="fillLogin('owner@test.com', 'password123')">
            <strong>
                Owner Account
                <span class="role-badge role-owner">OWNER</span>
            </strong>
            <small>owner@test.com / password123</small>
        </div>

        <div class="account-card" onclick="fillLogin('admin@test.com', 'password123')">
            <strong>
                Admin Account
                <span class="role-badge role-admin">ADMIN</span>
            </strong>
            <small>admin@test.com / password123</small>
        </div>
    </div>
</div>

<script>
    function fillLogin(email, password) {
        document.querySelector('input[name="email"]').value = email;
        document.querySelector('input[name="password"]').value = password;
    }
</script>
</body>
</html>