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

    <link href="https://fonts.googleapis.com/css2?family=Inter:wght=300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        /* ============================================
           BADMINTON PRO - ROOT VARIABLES
           ============================================ */
        :root {
            /* Brand Colors */
            --color-green-brand: #064E3B;
            --color-lime-brand: #A3E635;
            --color-green-50: #F0FDF4;
            --color-green-100: #DCFCE7;
            --color-green-200: #BBF7D0;
            --color-green-300: #86EFAC;
            --color-green-400: #4ADE80;
            --color-green-500: #22C55E;
            --color-green-600: #16A34A;
            --color-green-700: #15803D;
            --color-green-800: #166534;
            --color-green-900: #14532D;

            /* Neutral Colors */
            --color-gray-50: #F9FAFB;
            --color-gray-100: #F3F4F6;
            --color-gray-200: #E5E7EB;
            --color-gray-300: #D1D5DB;
            --color-gray-400: #9CA3AF;
            --color-gray-500: #6B7280;
            --color-gray-600: #4B5563;
            --color-gray-700: #374151;
            --color-gray-800: #1F2937;
            --color-gray-900: #111827;

            /* Accent Colors */
            --color-yellow-500: #EAB308;
            --color-yellow-600: #CA8A04;
            --color-red-500: #EF4444;
            --color-red-600: #DC2626;
            --color-red-700: #B91C1C;
            --color-blue-500: #3B82F6;
            --color-orange-400: #FB923C;
            --color-orange-600: #EA580C;

            /* Spacing */
            --spacing-xs: 0.25rem;
            --spacing-sm: 0.5rem;
            --spacing-md: 1rem;
            --spacing-lg: 1.5rem;
            --spacing-xl: 2rem;
            --spacing-2xl: 3rem;

            /* Border Radius */
            --radius-sm: 0.375rem;
            --radius-md: 0.5rem;
            --radius-lg: 0.75rem;
            --radius-xl: 1rem;
            --radius-2xl: 1.25rem;
            --radius-full: 9999px;

            /* Shadows */
            --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
            --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1);
            --shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1);
            --shadow-xl: 0 20px 25px -5px rgb(0 0 0 / 0.1);
            --shadow-2xl: 0 25px 50px -12px rgb(0 0 0 / 0.25);

            /* Typography */
            --font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;

            /* Transitions */
            --transition-fast: 150ms cubic-bezier(0.4, 0, 0.2, 1);
            --transition-base: 300ms cubic-bezier(0.4, 0, 0.2, 1);
            --transition-slow: 500ms cubic-bezier(0.4, 0, 0.2, 1);
        }

        body {
            font-family: var(--font-family);
            background-color: var(--color-gray-100);
        }
        .no-scrollbar::-webkit-scrollbar {
            display: none;
        }
        .no-scrollbar {
            -ms-overflow-style: none;
            scrollbar-width: none;
        }

        /* Brand Color Utilities */
        .bg-green-brand {
            background-color: var(--color-green-brand);
        }

        .bg-lime-brand {
            background-color: var(--color-lime-brand);
        }

        .text-green-brand {
            color: var(--color-green-brand);
        }

        .text-lime-brand {
            color: var(--color-lime-brand);
        }

        .border-gray-brand {
            border-color: var(--color-gray-100);
        }

        .bg-gray-brand {
            background-color: var(--color-gray-50);
        }
    </style>
</head>

<body onload="lucide.createIcons();">
    <div class="flex flex-col h-screen w-full bg-white overflow-hidden shadow-2xl">
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
        <div class="flex flex-1 overflow-hidden relative">
            <div class="w-full lg:w-96 flex-shrink-0 border-r border-gray-brand overflow-y-auto">
                <%@ include file="sidebar.jsp" %>
            </div>
            <div class="hidden lg:block flex-1 bg-gray-brand overflow-y-auto">
                <%
                    String section = (String) request.getAttribute("section");
                    if (section == null) {
                        section = request.getParameter("section");
                    }
                    if (section == null) {
                        section = "history";
                    }
                    switch (section) {
                        case "history":
                %> <%@ include file="customer_history.jsp" %> <%
                    break;
                case "booking-detail":
            %> <%@ include file="customer_booking_detail.jsp" %> <%
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
                case "favorites":
            %> <%@ include file="customer_favorite.jsp" %> <%
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
        <!-- Bottom Navigation -->
<%--    	<%@ include file="../../common/bottom-nav.jsp" %>--%>
        <%@ include file="bottomnav.jsp" %>

    </div>

    <script src="https://unpkg.com/lucide@latest"></script>

    <!-- Add SweetAlert2 -->
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <script src="<c:url value='/assets/js/alertPopup.js' />"></script>
    <script src="<c:url value='/assets/js/previewAvatar.js' />"></script>
</body>
</html>

