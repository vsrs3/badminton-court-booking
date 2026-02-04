<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Green Fropy</title>

    <link rel="stylesheet" href="assets/css/Header.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<header>
    <jsp:include page="../common/header.jsp"></jsp:include>
</header>

<main>

    <div class="register-container">
        <h2 class="register-title">REGISTER</h2>
        <!--
            <p class="register-subtitle" style="color: #333">Create an account to start shopping!</p>
        -->
        <form action="customerController" method="POST">
            <table class="form-group">
                <tbody>
                <tr>
                    <td><label for="username">Username:</label></td>
                    <td><input type="text" name="username" class="form-item" required /></td>
                </tr>
                <tr>
                    <td><label for="email">Email:</label></td>
                    <td><input type="email" name="email" class="form-item" required /></td>
                </tr>
                <tr>
                    <td><label for="password">Password:</label></td>
                    <td><input type="password" name="password" class="form-item" required /></td>
                </tr>
                <tr>
                    <td><label for="phone">Phone:</label></td>
                    <td><input type="tel" name="phone" class="form-item" required /></td>
                </tr>
                </tbody>
            </table>

            <button type="submit" name="action" value="register" class="btn-login">
                REGISTER
            </button>

            <div class="register-link">
                Already have an account? <a href="login">Login Now</a>
            </div>
        </form>

        <c:if test="${sessionScope.resError != null}">
            <p style="color: red">${sessionScope.resError}</p>
            <c:remove var="resError" scope="session"></c:remove>
        </c:if>
    </div>
</main>
</body>
</html>
