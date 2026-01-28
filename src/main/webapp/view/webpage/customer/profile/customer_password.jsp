<%--
    Document   : customer-changepass
    Created on : Jan 25, 2026, 3:50:47 PM
    Author     : dattr
--%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>JSP Page</title>
</head>
<body>
<section class="profile-section">
    <div class="section-header">
        <h2><i class="fas fa-lock"></i> Đổi mật khẩu</h2>
    </div>

    <c:if test="${not empty sessionScope.updateSuccess}">
        <div class="alert alert-success">
            <i class="fas fa-check-circle"></i>
            <span>${sessionScope.updateSuccess}</span>
        </div>
        <c:remove var="updateSuccess" scope="session"/>
    </c:if>

    <c:if test="${not empty sessionScope.updateError}">
        <div class="alert alert-error">
            <i class="fas fa-exclamation-circle"></i>
            <span>${sessionScope.updateError}</span>
        </div>
        <c:remove var="updateError" scope="session"/>
    </c:if>

    <form action="customerController" method="post" class="profile-form">
        <div class="form-group">
            <label for="oldPassword"><i class="fas fa-key"></i> Mật khẩu hiện tại</label>
            <div class="password-input-wrapper">
                <input type="password" id="oldPassword" name="oldPassword" required class="form-input" placeholder="Nhập mật khẩu hiện tại">
                <button type="button" class="toggle-password" onclick="togglePassword('oldPassword')">
                    <i class="fas fa-eye"></i>
                </button>
            </div>
        </div>

        <div class="form-group">
            <label for="newPassword"><i class="fas fa-lock"></i> Mật khẩu mới</label>
            <div class="password-input-wrapper">
                <input type="password" id="newPassword" name="newPassword" required minlength="6" class="form-input" placeholder="Nhập mật khẩu mới (tối thiểu 6 ký tự)">
                <button type="button" class="toggle-password" onclick="togglePassword('newPassword')">
                    <i class="fas fa-eye"></i>
                </button>
            </div>
            <small class="form-hint">Mật khẩu phải có ít nhất 6 ký tự</small>
        </div>

        <div class="form-group">
            <label for="confirmPassword"><i class="fas fa-lock"></i> Xác nhận mật khẩu mới</label>
            <div class="password-input-wrapper">
                <input type="password" id="confirmPassword" name="confirmPassword" required class="form-input" placeholder="Nhập lại mật khẩu mới">
                <button type="button" class="toggle-password" onclick="togglePassword('confirmPassword')">
                    <i class="fas fa-eye"></i>
                </button>
            </div>
        </div>

        <div class="form-actions">
            <button type="submit" name="action" value="updatePassword" class="btn btn-primary">
                <i class="fas fa-check"></i> Đổi mật khẩu
            </button>
            <button type="reset" class="btn btn-secondary">
                <i class="fas fa-times"></i> Hủy bỏ
            </button>
        </div>
    </form>
</section>
</body>
</html>
