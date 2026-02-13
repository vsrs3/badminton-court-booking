<%--
  Created by IntelliJ IDEA.
  User: Nguyen Minh Duc
  Date: 04/02/2026
  Time: 11:04 CH
  To change this template use File | Settings | File Templates.
--%>


<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String token = request.getParameter("token");
    if (token == null) {
        token = "";
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Kiểm tra email</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f6f7fb;
        }

        .container {
            width: 480px;
            margin: 80px auto;
            background: #fff;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
            text-align: center;
        }

        h2 {
            color: #2c3e50;
        }

        p {
            font-size: 15px;
            color: #555;
            line-height: 1.6;
        }

        .note {
            font-size: 13px;
            color: #999;
            margin-top: 15px;
        }

        .countdown {
            font-size: 18px;
            font-weight: bold;
            color: #e74c3c;
        }

        .btn {
            display: inline-block;
            margin-top: 25px;
            padding: 10px 20px;
            background: #3498db;
            color: white;
            text-decoration: none;
            border-radius: 4px;
        }.btn:hover {
             background: #2980b9;
         }
    </style>
</head>
<body>

<div class="container">
    <h2>📧 Kiểm tra email của bạn</h2>
    <p>
        Chúng tôi đã gửi một email xác nhận đến địa chỉ bạn vừa đăng ký.
    </p>
    <p>
        Vui lòng mở email và <b>nhấn vào link xác nhận</b> để hoàn tất việc tạo tài khoản.
    </p>

    <p class="note">
        ⏳ Link xác nhận chỉ có hiệu lực trong <b>60 giây</b>.
    </p>
    <p class="note">
        Trang sẽ tự quay lại đăng ký sau
        <span class="countdown" id="countdown">60</span> giây.
    </p>
</div>

<script>
    let seconds = 60;
    const token = "<%= token %>";
    const countdownEl = document.getElementById("countdown");

    // Đếm ngược realtime
    const interval = setInterval(() => {
        seconds--;
        if (countdownEl) {
            countdownEl.innerText = seconds;
        }
        if (seconds <= 0) {
            clearInterval(interval);
        }
    }, 1000);
    // Sau 10s: cleanup + redirect
    setTimeout(() => {
        if (token && token !== "") {
            fetch("<%= request.getContextPath() %>/cleanup-email", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: "token=" + encodeURIComponent(token)
            }).finally(() => {
                window.location.href = "${pageContext.request.contextPath}/jsp/auth/register.jsp";
            });
        } else {
            window.location.href = "${pageContext.request.contextPath}/jsp/auth/register.jsp";
        }
    }, 60000);
</script>
</body>
</html>

