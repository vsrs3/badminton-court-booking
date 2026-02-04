<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bcb.model.Account" %>
<%
    Account customer = (Account) session.getAttribute("account");
    if (customer == null || !"CUSTOMER".equals(customer.getRole())) {
        response.sendRedirect(request.getContextPath() + "/auth/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ sơ cá nhân - BadmintonPro</title>

    <!-- Bootstrap 5.3 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
</head>
<body style="background-color: #F9FAFB;">

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card border-0 shadow-sm">
                <div class="card-body p-5 text-center">
                    <h1 class="mb-4">🎾 Hồ sơ cá nhân</h1>
                    <p class="lead">Chức năng đang được phát triển</p>
                    <p><strong>Tên:</strong> <%= customer.getFullName() %></p>
                    <p><strong>Email:</strong> <%= customer.getEmail() %></p>
                    <p><strong>Vai trò:</strong> <%= customer.getRole() %></p>

                    <a href="${pageContext.request.contextPath}/" class="btn btn-success mt-4">
                        <i class="bi bi-arrow-left"></i> Về trang chủ
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>