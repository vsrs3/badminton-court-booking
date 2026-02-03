<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Đăng nhập</title>
    <style>
        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #0f9b4b, #2ecc71);
            height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .login-box {
            background: #fff;
            width: 420px;
            border-radius: 12px;
            padding: 30px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
        }

        .login-box h2 {
            text-align: center;
            margin-bottom: 25px;
            color: #2e7d32;
        }

        .form-group {
            margin-bottom: 18px;
        }

        .form-group label {
            display: block;
            margin-bottom: 6px;
            font-weight: bold;
        }

        .form-group input {
            width: 100%;
            padding: 12px;
            border-radius: 6px;
            border: 1px solid #ccc;
            font-size: 14px;
        }

        .btn-login {
            width: 100%;
            background: #0f9b4b;
            color: white;
            border: none;
            padding: 14px;
            font-size: 16px;
            border-radius: 8px;
            cursor: pointer;
        }

        .btn-login:hover {
            background: #0c7c3b;
        }

        .error {
            color: red;
            text-align: center;
            margin-top: 10px;
        }

        .extra {
            text-align: center;
            margin-top: 15px;
            font-size: 14px;
        }

        .extra a {
            color: #0f9b4b;
            text-decoration: none;
            font-weight: bold;
        }

        .divider {
            text-align: center;
            margin: 25px 0;
            position: relative;
        }

        .divider::before,
        .divider::after {
            content: "";
            position: absolute;
            width: 40%;
            height: 1px;
            background: #ccc;
            top: 50%;
        }

        .divider::before {
            left: 0;
        }

        .divider::after {
            right: 0;
        }

        .google-btn {
            width: 100%;
            border: 1px solid #ccc;
            background: #fff;
            padding: 12px;
            border-radius: 8px;
            font-size: 15px;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
        }

        .google-btn img {
            width: 20px;
        }

        .google-btn:hover {
            background: #f5f5f5;
        }
    </style>
</head>
<body>

<div class="login-box">
    <h2>Đăng nhập</h2>

    <form action="login" method="post">
        <div class="form-group">
            <label>Email</label>
            <input type="email" name="email" placeholder="Nhập email của bạn" required>
        </div>

        <div class="form-group">
            <label>Mật khẩu</label>
            <input type="password" name="password" placeholder="Nhập mật khẩu" required>
        </div>

        <button type="submit" class="btn-login">ĐĂNG NHẬP</button>
    </form>

    <div class="error">
        ${error}
    </div>

    <div class="extra">
        Bạn chưa có tài khoản? <a href="register.jsp">Đăng ký</a>
    </div>
    <div class="divider">hoặc</div>

    <a href="google-login" style="text-decoration:none;">
        <button class="google-btn" type="button">
            <img src="https://developers.google.com/identity/images/g-logo.png">
            Đăng nhập với Google
        </button>
    </a>
</div>

</body>
</html>
