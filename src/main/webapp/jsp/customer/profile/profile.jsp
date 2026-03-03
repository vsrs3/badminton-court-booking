<!-- profile-page.jsp -->

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Badminton Profile</title>
    <script src="https://cdn.tailwindcss.com"></script>

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer/customer-profile-page.css">

    <link href="https://fonts.googleapis.com/css2?family=Inter:wght=300;400;500;600;700&display=swap" rel="stylesheet">
   
</head>

<body onload="lucide.createIcons();">
    <div class="flex flex-col h-screen w-full bg-white shadow-2xl">
    
        <header class="lg:hidden bg-green-brand text-white p-4 flex items-center justify-between">
            <div class="flex items-center space-x-2">
                <div class="w-8 h-8 bg-lime-brand rounded-full flex items-center justify-center">
                    <span class="text-green-brand font-bold text-xs">▲</span>
                </div>
                <h1 class="font-bold text-sm tracking-tight uppercase">CHỌN SÂN CHƠI NGAY</h1>
            </div>
            <div class="flex space-x-2">
                <button class="bg-lime-brand text-green-brand px-3 py-1 rounded-md text-xs font-bold">ĐĂNG NHẬP</button>
            </div>
        </header>
        
        <div class="flex flex-1 min-h-0">
        
            <div class="w-full lg:w-96 flex-shrink-0 border-r border-gray-brand overflow-y-auto">
                <%@ include file="sidebar.jsp" %>
            </div>
            
            <div class="hidden lg:block flex-1 min-h-0 bg-gray-brand overflow-y-auto">
                <%
                    String section = request.getParameter("section");
                    if (section == null) {
                        section = "history";
                    }
               	switch (section) {
                        case "history":
                %> <%@ include file="customer_history.jsp" %> <%
                    break;
                case "settings":
            %> <%@ include file="customer_settings.jsp" %> <%
                    break;
                case "profile-info":
            %> <%@ include file="customer_view.jsp" %> <%
                    break;
                case "change-password":
            %> <%@ include file="change_password.jsp" %> <%
                    break;
                case "notifications":
            %> <%@ include file="notifications.jsp" %> <%
                    break;
                case "languages":
            %> <%@ include file="languages-setting.jsp" %> <%
                    break;
                default:
            %> <div class="p-6">Phần không tồn tại</div> <%
                }
            %>
            </div>
        </div>
<<<<<<< Updated upstream
        <!-- Bottom Navigation -->
    	<%@ include file="../../common/bottom-nav.jsp" %>
=======
>>>>>>> Stashed changes

    </div>
    
	<!--  -->
    <script src="https://unpkg.com/lucide@latest"></script>

    <!-- Add SweetAlert2 -->
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    
    <!-- JS load -->
    <script src="<c:url value='/assets/js/alertPopup.js' />"></script>
    <script src="<c:url value='/assets/js/previewAvatar.js' />"></script>
    <script src="<c:url value='/assets/js/badminton-pro.js' />"></script>
    
</body>
</html>