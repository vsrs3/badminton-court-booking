<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- 1. Include Layout --%>
<%@ include file="layout/layout.jsp" %>

<%-- 2. Include Sidebar (Fixed Position) --%>
<%@ include file="layout/sidebar.jsp" %>

<%-- 3. MỞ Main Wrapper (Footer sẽ đóng thẻ này) --%>
<div class="main-content">

    <%-- PAGE HEADER --%>
    <%@ include file="layout/header.jsp" %>

    <div class="content-area">

        <%-- Set title cho page --%>
        <c:set var="pageTitle" value="Dashboard" />
        <%@ include file="layout/page-header.jsp" %>

        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-danger alert-dismissible fade show">
                <i class="bi bi-exclamation-triangle-fill me-2"></i> ${requestScope.error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <div class="row g-4 mb-4">
            <div class="col-12 col-md-6 col-xl-3">
                <div class="stat-card">
                    <div class="stat-icon bg-emerald"><i class="bi bi-building"></i></div>
                    <div>
                        <h6 class="text-muted mb-1 small">Total Locations</h6>
                        <h3 class="mb-0 fw-bold">${requestScope.totalLocations != null ? requestScope.totalLocations : 0}</h3>
                    </div>
                </div>
            </div>

            <div class="col-12 col-md-6 col-xl-3">
                <div class="stat-card">
                    <div class="stat-icon bg-info"><i class="bi bi-grid-3x3"></i></div>
                    <div>
                        <h6 class="text-muted mb-1 small">Total Courts</h6>
                        <h3 class="mb-0 fw-bold">${requestScope.totalCourts != null ? requestScope.totalCourts : 0}</h3>
                    </div>
                </div>
            </div>

            <div class="col-12 col-md-6 col-xl-3">
                <div class="stat-card">
                    <div class="stat-icon bg-success"><i class="bi bi-check-circle"></i></div>
                    <div>
                        <h6 class="text-muted mb-1 small">Active Courts</h6>
                        <h3 class="mb-0 fw-bold">${requestScope.activeCourts != null ? requestScope.activeCourts : 0}</h3>
                    </div>
                </div>
            </div>

            <div class="col-12 col-md-6 col-xl-3">
                <div class="stat-card">
                    <div class="stat-icon bg-warning"><i class="bi bi-calendar-check"></i></div>
                    <div>
                        <h6 class="text-muted mb-1 small">Monthly Bookings</h6>
                        <h3 class="mb-0 fw-bold">${requestScope.monthlyBookings != null ? requestScope.monthlyBookings : 0}</h3>
                    </div>
                </div>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <h5 class="mb-0">Quick Actions</h5>
            </div>
            <div class="card-body">
                <a href="${pageContext.request.contextPath}/owner/facility/list" class="btn btn-accent me-2">
                    <i class="bi bi-plus-circle me-1"></i> Manage Locations
                </a>
                <button class="btn btn-outline-secondary">
                    <i class="bi bi-gear me-1"></i> System Settings
                </button>
            </div>
        </div>

    </div> <%-- 4. Include Footer (Đóng .main-content, đóng body) --%>
<%@ include file="layout/footer.jsp" %>
