<%-- staff-sidebar.jsp — Updated Task 6: Navigation polish --%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%-- Determine active page from servlet path --%>
<%
    String servletPath = (String) request.getAttribute("jakarta.servlet.forward.servlet_path");
    String pathInfo = (String) request.getAttribute("jakarta.servlet.forward.path_info");
    if (servletPath == null) servletPath = request.getServletPath();
    if (pathInfo == null) pathInfo = request.getPathInfo();
    String fullPath = (servletPath != null ? servletPath : "") + (pathInfo != null ? pathInfo : "");

    String activePage = "timeline"; // default
    if (fullPath.contains("/booking/list")) {
        activePage = "booking-list";
    } else if (fullPath.contains("/booking/detail")) {
        // Booking detail: came from timeline or list — check referer
        String ref = request.getHeader("Referer");
        if (ref != null && ref.contains("/booking/list")) {
            activePage = "booking-list";
        } else {
            activePage = "timeline";
        }
    } else if (fullPath.contains("/timeline")) {
        activePage = "timeline";
    }
    request.setAttribute("_activePage", activePage);
%>

<%-- Sidebar backdrop for mobile --%>
<div class="st-sidebar-backdrop" id="stSidebarBackdrop"
     onclick="closeSidebar()"></div>

<div class="sidebar" id="stSidebar">

    <%-- Sidebar Header --%>
    <div class="sidebar-header">
        <a href="${pageContext.request.contextPath}/staff/timeline"
           class="sidebar-brand">
            <i class="bi bi-circle-fill" style="color:#A3E635"></i>
            <span style="color:#A3E635">BCB Staff</span>
        </a>
        <%-- Close button for mobile --%>
        <button class="btn btn-link text-white d-lg-none ms-auto p-0"
                onclick="closeSidebar()"
                style="font-size:1.25rem;line-height:1;">
            <i class="bi bi-x-lg"></i>
        </button>
    </div>

    <%-- Sidebar Navigation --%>
    <div class="sidebar-nav">

        <%-- Facility name display --%>
        <c:if test="${not empty sessionScope.facilityName}">
            <div class="px-3 py-2 mb-3"
                 style="background:rgba(163,230,53,0.08); border-radius:0.5rem;">
                <div class="d-flex align-items-center gap-2">
                    <i class="bi bi-building" style="color:#A3E635; font-size:1rem;"></i>
                    <div style="overflow:hidden;">
                        <p class="mb-0 text-truncate"
                           style="font-size:0.6875rem; font-weight:800; color:rgba(255,255,255,0.5);
                                  text-transform:uppercase; letter-spacing:0.1em;">
                            Cơ sở
                        </p>
                        <p class="mb-0 text-truncate"
                           style="font-size:0.8125rem; font-weight:700; color:white;">
                            <c:out value="${sessionScope.facilityName}"/>
                        </p>
                    </div>
                </div>
            </div>
        </c:if>

        <%-- Nav: Lịch đặt sân (Timeline) --%>
        <a href="${pageContext.request.contextPath}/staff/timeline"
           class="nav-link ${_activePage == 'timeline' ? 'active' : ''}">
            <i class="bi bi-calendar3"></i>
            <span>Lịch Đặt Sân</span>
        </a>

        <%-- Nav: Danh sách booking --%>
        <a href="${pageContext.request.contextPath}/staff/booking/list"
           class="nav-link ${_activePage == 'booking-list' ? 'active' : ''}">
            <i class="bi bi-list-ul"></i>
            <span>Danh Sách Booking</span>
        </a>

        <%-- Divider + Logout --%>
        <div class="mt-auto pt-4">
            <hr style="border-color:rgba(255,255,255,0.08); margin:0 0 0.75rem;">
            <a href="${pageContext.request.contextPath}/auth/logout"
               class="nav-link"
               style="color:rgba(255,255,255,0.6);">
                <i class="bi bi-box-arrow-right"></i>
                <span>Đăng Xuất</span>
            </a>
        </div>

    </div>
</div>

<script>
    function openSidebar() {
        document.getElementById('stSidebar').classList.add('show');
        document.getElementById('stSidebarBackdrop').classList.add('active');
        document.body.style.overflow = 'hidden';
    }
    function closeSidebar() {
        document.getElementById('stSidebar').classList.remove('show');
        document.getElementById('stSidebarBackdrop').classList.remove('active');
        document.body.style.overflow = '';
    }
</script>