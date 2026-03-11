<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Favorite Facilities</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css">
</head>
<body onload="lucide.createIcons();">
<div class="flex flex-col h-screen w-full bg-white overflow-hidden shadow-2xl">
    <div class="flex flex-1 overflow-hidden relative">
        <div class="w-full lg:w-96 flex-shrink-0 border-r border-gray-100 overflow-y-auto">
            <%@ include file="sidebar.jsp" %>
        </div>
        <div class="flex-1 bg-gray-50 overflow-y-auto">
            <%@ include file="customer_favorite.jsp" %>
        </div>
    </div>
    <%@ include file="bottomnav.jsp" %>
</div>

<script src="https://unpkg.com/lucide@latest"></script>
</body>
</html>
