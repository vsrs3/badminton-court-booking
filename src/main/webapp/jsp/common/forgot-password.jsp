<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quên mật khẩu</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            min-height: 100vh;
            margin: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            background: linear-gradient(135deg, #6dd5fa, #2980b9);
        }

        .card {
            width: 420px;
            background: #ffffff;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 15px 35px rgba(0,0,0,0.25);
        }

        .error {
            background: #ffe6e6;
            border: 1px solid #ff4d4d;
            color: #c0392b;
            padding: 10px;
            margin-bottom: 15px;
            border-radius: 4px;
            font-size: 14px;
        }

        input {
            width: 100%;
            padding: 8px;
            box-sizing: border-box;
        }

        button {
            padding: 8px 16px;
            cursor: pointer;
            margin-top: 10px;
        }

        .eye {
            position: absolute;
            right: 10px;
            top: 50%;
            transform: translateY(-50%);
            cursor: pointer;
            user-select: none;
        }

        .rule-container {
            display: none;
            margin-top: 8px;
            font-size: 13px;
            display: none;
            flex-wrap: wrap;
            gap: 10px;
        }

        .rule-container span {
            color: red;
        }

        .match-error {
            color: red;
            font-size: 13px;
            margin-top: 6px;
            display: none;
        }
    </style>
</head>

<body>
<div class="card">

    <h3>Quên mật khẩu</h3>

    <% if (request.getAttribute("error") != null) { %>
    <div class="error">
        <%= request.getAttribute("error") %>
    </div>
    <% } %>
    <%
        String step = (String) request.getAttribute("step");
        if (step == null) {
    %>

    <!-- =========================
         BƯỚC 1: NHẬP EMAIL
    ========================== -->
    <form method="post" action="${pageContext.request.contextPath}/forgot-password">
        <input type="hidden" name="action" value="checkEmail"/>

        <label>Vui lòng nhập email</label><br>
        <input type="email" name="email" required/><br><br>

        <button type="submit">Xác nhận</button>
    </form>
    <% } else { %>

    <!-- =========================
         BƯỚC 2: RESET PASSWORD
    ========================== -->
    <form method="post"
          action="${pageContext.request.contextPath}/forgot-password"
          onsubmit="return validateForm()">

        <input type="hidden" name="action" value="reset"/>
        <input type="hidden" name="email"
               value="<%= request.getAttribute("email") %>"/>

        <!-- PASSWORD -->
        <label>Mật khẩu mới</label>
        <div style="position:relative;">
            <input type="password" id="password" name="password" required/>
            <span class="eye"
                  onclick="togglePassword('password', this)">👁</span>
        </div>
        <!-- RULES -->
        <div id="ruleContainer" class="rule-container">
            <span id="ruleLength">≥ 8 ký tự</span>
            <span id="ruleUpper">Ít nhất 1 chữ in hoa</span>
            <span id="ruleNumber">Ít nhất 1 chữ số</span>
            <span id="ruleSpecial">Ít nhất 1 ký tự đặc biệt</span>
        </div>
        <br>
        <!-- CONFIRM PASSWORD -->
        <label>Nhập lại mật khẩu</label>
        <div style="position:relative;">
            <input type="password" id="repassword" name="repassword" required/>
            <span class="eye"
                  onclick="togglePassword('repassword', this)">👁</span>
        </div>
        <div id="matchError" class="match-error">
            Mật khẩu không khớp
        </div>
        <button type="submit">Lưu mật khẩu</button>
    </form>
    <% } %>
</div>
<script>
    function togglePassword(inputId, icon) {
        const input = document.getElementById(inputId);
        if (input.type === "password") {
            input.type = "text";
            icon.innerText = "🙈";
        } else {
            input.type = "password";
            icon.innerText = "👁";
        }
    }
    function validateForm() {
        const pw = document.getElementById("password").value;
        const repw = document.getElementById("repassword").value;
        const ruleContainer = document.getElementById("ruleContainer");
        const ruleLength = document.getElementById("ruleLength");
        const ruleUpper = document.getElementById("ruleUpper");
        const ruleNumber = document.getElementById("ruleNumber");
        const ruleSpecial = document.getElementById("ruleSpecial");
        const matchError = document.getElementById("matchError");
        let isValid = true;
        ruleContainer.style.display = "flex";
        // ≥ 8 ký tự
        if (pw.length < 8) {
            ruleLength.style.display = "inline";
            isValid = false;
        } else {
            ruleLength.style.display = "none";
        }
        // 1 chữ in hoa
        if (!/[A-Z]/.test(pw)) {
            ruleUpper.style.display = "inline";
            isValid = false;
        } else {
            ruleUpper.style.display = "none";
        }// 1 chữ số
        if (!/[0-9]/.test(pw)) {
            ruleNumber.style.display = "inline";
            isValid = false;
        } else {
            ruleNumber.style.display = "none";
        }
        // 1 ký tự đặc biệt
        if (!/[^A-Za-z0-9]/.test(pw)) {
            ruleSpecial.style.display = "inline";
            isValid = false;
        } else {
            ruleSpecial.style.display = "none";
        }
        // Confirm password
        if (pw !== repw) {
            matchError.style.display = "block";
            isValid = false;
        } else {
            matchError.style.display = "none";
        }
        return isValid;
    }
</script>
</body>
</html>
