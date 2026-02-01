<%--
    Document   : customer_info
    Created on : Jan 25, 2026, 3:43:41 PM
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
        <h2><i class="fas fa-user"></i> Thông tin cá nhân</h2>
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

    <form action="customerController" method="post" enctype="multipart/form-data" class="profile-form">
        <div class="avatar-upload">
            <div class="avatar-preview">
                <c:choose>
                    <c:when test="${not empty sessionScope.customer.avatarPath}">
                        <img src="${pageContext.request.contextPath}/${sessionScope.customer.avatarPath}"
                             alt="Avatar" id="avatarPreview">
                    </c:when>
                    <c:otherwise>
                        <i class="fas fa-user-circle" id="avatarIcon"></i>
                    </c:otherwise>
                </c:choose>
            </div>
            <div>
                <input type="file" id="avatar" name="avatar" accept="image/*" class="form-input-file" onchange="previewAvatar(this)">
                <label for="avatar" class="btn-upload">
                    <i class="fas fa-camera"></i> Chọn ảnh đại diện
                </label>
                <p class="form-hint"><i class="fas fa-info-circle"></i> JPG, PNG tối đa 2MB</p>
            </div>
        </div>

        <div class="form-group">
            <label for="full_name"><i class="fas fa-user"></i> Họ và tên</label>
            <input type="text" id="full_name" name="full_name" value="${sessionScope.customer.fullName}" required class="form-input">
        </div>

        <div class="form-group">
            <label for="email"><i class="fas fa-envelope"></i> Email</label>
            <input type="email" id="email" name="email" value="${sessionScope.customer.email}" required class="form-input"
                   <c:if test="${not empty sessionScope.customer.googleId}">readonly</c:if>>
            <c:if test="${not empty sessionScope.customer.googleId}">
                <small class="form-hint"><i class="fab fa-google"></i> Đăng nhập bằng Google</small>
            </c:if>
        </div>

        <div class="form-group">
            <label for="phone"><i class="fas fa-phone"></i> Số điện thoại</label>
            <input type="tel" id="phone" name="phone" value="${sessionScope.customer.phone}"
                   pattern="[0-9]{10,11}" class="form-input" placeholder="Nhập số điện thoại">
        </div>

        <div class="form-actions">
            <button type="submit" name="action" value="updateProfile" class="btn btn-primary">
                <i class="fas fa-save"></i> Lưu thay đổi
            </button>
            <button type="reset" class="btn btn-secondary">
                <i class="fas fa-undo"></i> Khôi phục
            </button>
        </div>
    </form>
</section>
</body>
</html>
