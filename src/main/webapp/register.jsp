<%-- 
    Document   : register
    Created on : Jan 26, 2026, 2:18:51 PM
    Author     : Nguyen Minh Duc
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Đăng ký tài khoản</title>
    <style>

form {
    width: 100%;
}


.form-group {
    margin-bottom: 14px;
    position: relative;
}

.form-group input {
    width: 100%;
    padding: 8px;
    box-sizing: border-box;
}

/* lỗi inline */
.error {
    color: red;
    font-size: 12px;
    display: none;
    margin-top: 4px;
}

/* password eye */
.eye {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
    cursor: pointer;
    user-select: none;
}
.alert {
    background: #ffe6e6;
    border: 1px solid #ff4d4d;
    color: #c0392b;
    padding: 10px;
    margin-bottom: 15px;
    border-radius: 4px;
    font-size: 14px;
}

.alert {
    transition: opacity 0.5s;
}
button {
    padding: 8px 16px;
    cursor: pointer;
}

body {
    font-family: Arial, sans-serif;
    min-height: 100vh;
    margin: 0;

    /* 👇 3 dòng quyết định */
    display: flex;
    justify-content: center;   /* căn giữa ngang */
    align-items: center;       /* căn giữa dọc */

    background: linear-gradient(135deg, #6dd5fa, #2980b9);
}

.register-card {
    width: 420px;
    background: #ffffff;
    padding: 30px 32px;
    border-radius: 10px;
    box-shadow: 0 15px 35px rgba(0,0,0,0.25);
}

</style>
    
</head>

<body>
<div class="register-card">
    
<% if (request.getAttribute("error") != null) { %>
    <div class="alert" id="alertBox">
        <%= request.getAttribute("error") %>
    </div>
<% } %>


<h3>Đăng ký tài khoản</h3>
<form action="register" method="post" onsubmit="return validateForm()">
    <!-- EMAIL -->
    <div class="form-group">
    <label><span style="color:red">*</span> Email</label>
    <input type="text" id="email" name="email"value="${oldEmail != null ? oldEmail : ''}">
    <div class="error" id="emailError"></div>
</div>


    <!-- PASSWORD -->
    <div class="form-group">
    <label><span style="color:red">*</span> Mật khẩu</label>
    <input type="password" id="password" name="password"value="${oldPasword != null ? oldPasword : ''}">
    <span class="eye" onclick="togglePassword()">👁</span>
    <div class="error" id="pwError"></div>
</div>


    <!-- CONFIRM PASSWORD -->
   <div class="form-group">
    <label><span style="color:red">*</span> Nhập lại mật khẩu</label>
    <input type="password" id="repassword" name="repassword"value="${oldPasword != null ? oldPasword : ''}">
    <span class="eye" onclick="togglePassword()">👁</span>
    <div class="error" id="rePwError"></div>
</div>


    <!-- FULL NAME -->
    <div class="form-group">
    <label><span style="color:red">*</span> Họ và tên</label>
    <input type="text" id="fullName" name="fullName"value="${oldFullName != null ? oldFullName : ''}">
    <div class="error" id="nameError"></div>
</div>

    <!-- PHONE (OPTIONAL) -->
    <div class="form-group">
    <label>Số điện thoại (không bắt buộc)</label>
    <input type="text" id="phone" name="phone"value="${oldPhone != null ? oldPhone : ''}">
    <div class="error" id="phoneError"></div>
</div>


    <button type="submit">Đăng ký</button>
</form>

    <script>       
        // Validate số điện thoại
      function togglePassword() {
    const pw = document.getElementById("password");
    const repw = document.getElementById("repassword");
    const type = pw.type === "password" ? "text" : "password";
    pw.type = type;
    repw.type = type;
}

    function showError(id, msg) {
    const el = document.getElementById(id);
    el.innerText = msg;
    el.style.display = "block";
}

function hideErrors() {
    document.querySelectorAll(".error").forEach(e => {
        e.style.display = "none";
    });
}

function validateForm() {
    hideErrors();
    let ok = true;

    const email = emailEl = document.getElementById("email").value.trim();
    const pw = document.getElementById("password").value;
    const repw = document.getElementById("repassword").value;
    const name = document.getElementById("fullName").value.trim();
    const phone = document.getElementById("phone").value.trim();

    // EMAIL
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    showError(
        "emailError",
        "Email không hợp lệ. Ví dụ: tennguoidung@gmail.com"
    );
    ok = false;
}


    // PASSWORD
    if (
        pw.length < 8 ||
        !/[A-Z]/.test(pw) ||
        !/[0-9]/.test(pw) ||
        !/[^A-Za-z0-9]/.test(pw)
    ) {
        showError(
            "pwError",
            "Mật khẩu ≥8 ký tự, có in hoa, số và ký tự đặc biệt"
        );
        ok = false;
    }

    // CONFIRM
    if (pw !== repw) {
        showError("rePwError", "Mật khẩu không khớp");
        ok = false;
    }

    // FULL NAME
    if (name === "") {
        showError("nameError", "Họ tên không được để trống");
        ok = false;
    }

    // PHONE (OPTIONAL)
    if (phone !== "" && !/^[0-9]{10}$/.test(phone)) {
    showError("phoneError", "Số điện thoại phải gồm đúng 10 chữ số");
    ok = false;
}

    return ok;
}

    </script>

    <script>
window.addEventListener("DOMContentLoaded", () => {
    const alertBox = document.getElementById("alertBox");
    if (alertBox) {
        setTimeout(() => {
            alertBox.style.transition = "opacity 0.5s";
            alertBox.style.opacity = "0";
            setTimeout(() => alertBox.remove(), 500);
        }, 5000); // 5 giây
    }
});
</script>

</div>
</body>
</html>
