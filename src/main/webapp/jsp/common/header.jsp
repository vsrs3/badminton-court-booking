<%--
    Document   : header
    Created on : Jan 24, 2026, 2:54:24 PM
    Author     : dattr
--%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>JSP - Hello</title>

    <!-- Load Font Awesome via CDN -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<!-- Nav -->
<nav class="nav-bar">
    <a class="nav-branding" href="home">Fropy</a>
    <div>
        <ul class="nav-menu">
            <li class="nav-item">
                <a class="nav-link" href="home">Home</a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="#">About</a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="product?category=0">Product</a>
            </li>
        </ul>
        <div class="hamburger">
            <span class="bar"></span>
            <span class="bar"></span>
            <span class="bar"></span>
        </div>
    </div>
    <div class="nav-icon">
        <form action="product" method="GET" class="search-form">
            <input type="text" name="search" class="search-input" placeholder="Search products...">
            <button type="submit" class="search-btn" style="background-color: #262626">
                <i class="fa fa-search icon-item"></i>
            </button>
        </form>
        <a class="nav-link">
            <i class="fa fa-cart-arrow-down icon-item"></i>
        </a>
        <c:if test="${sessionScope.account == null}">
            <a class="nav-link" href="login">
                <i class="fa fa-user icon-item"></i>
            </a>
        </c:if>
        <c:if test="${sessionScope.account != null}">
            <a class="nav-link" href="profile?section=history">
                <i class="fa fa-user icon-item"></i>
            </a>
        </c:if>
    </div>
</nav>
</body>
</html>
