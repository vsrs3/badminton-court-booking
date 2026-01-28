<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>JSP Page</title>

    <link rel="stylesheet" href="assets/css/Header.css">
    <link rel="stylesheet" href="assets/css/Footer.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<header>
    <jsp:include page="../common/header.jsp"></jsp:include>
</header>

<c:if test="${sessionScope.logError != null}">
    <p style="color: red">${sessionScope.logError}</p>
</c:if>
<main>
    <h2>LOGIN</h2>
    <form method="POST" action="customerController"> <!-- autocomplete="off" -->
        <table class="form-group">
            <tbody>
            <tr>
                <td>Email:</td>
                <td>
                    <input type="text" class="form-item" name="email" placeholder="email" >
                </td>
            </tr>
            </tbody>
        </table>
        <button type="submit" name="action" value="login" class="btn-login">LOGIN</button>
        <div class="register-link">
            No Account ?<a href="register">Register Now</a>
        </div>
    </form>
</main>

<footer>
    <jsp:include page="../common/footer.jsp"></jsp:include>
</footer>
</body>
</html>
