<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%-- LAYOUT INCLUDES --%>
<%@ include file="../layout/layout.jsp" %>
<%@ include file="../layout/sidebar.jsp" %>

<div class="main-content">
    <%-- GLOBAL HEADER --%>
    <%@ include file="../layout/header.jsp" %>

    <div class="content-area">

        <%-- PAGE HEADER --%>
<%--        <div class="d-flex justify-content-between align-items-center mb-4">--%>
<%--            <div>--%>
<%--                <h2 class="h3 mb-1 text-emerald">Cài đặt giá</h2>--%>
<%--                <p class="text-muted mb-0">Cài đặt giá theo giờ cho <strong>${viewData.facilityName}</strong></p>--%>
<%--            </div>--%>
<%--            <div>--%>
<%--                <a href="${pageContext.request.contextPath}/owner/facility/view/${viewData.facilityId}" class="btn btn-outline-secondary">--%>
<%--                    <i class="bi bi-arrow-left me-1"></i> Quay lại xem chi tiết--%>
<%--                </a>--%>
<%--            </div>--%>
<%--        </div>--%>
            <%-- PAGE HEADER (breadcrumb + title set by controller) --%>
            <%@ include file="../layout/page-header.jsp" %>

        <%-- ALERTS - Flash Messages from Session --%>
        <div id="alertContainer">
            <%-- Error Flash Message --%>
            <c:if test="${not empty sessionScope.flashError}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    <i class="bi bi-exclamation-circle me-2"></i> ${sessionScope.flashError}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <%-- Remove from session after displaying --%>
                <c:remove var="flashError" scope="session"/>
            </c:if>

            <%-- Success Flash Message --%>
            <c:if test="${not empty sessionScope.flashSuccess}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    <i class="bi bi-check-circle me-2"></i> ${sessionScope.flashSuccess}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <%-- Remove from session after displaying --%>
                <c:remove var="flashSuccess" scope="session"/>
            </c:if>
        </div>

        <%-- CONFIGURATION BAR --%>
        <div class="card mb-4">
            <div class="card-body p-3">
                <div class="row align-items-center g-3">
                    <%-- COURT TYPE TABS --%>
                    <div class="col-md-5">
                        <ul class="nav nav-pills custom-pills" id="courtTypeTabs">
                            <c:forEach items="${viewData.courtTypes}" var="type">
                                <li class="nav-item">
                                    <a class="nav-link ${viewData.currentCourtTypeId == type.courtTypeId ? 'active' : ''}"
                                       href="?facilityId=${viewData.facilityId}&courtTypeId=${type.courtTypeId}&dayType=${viewData.currentDayType}">
                                        SÂN ${type.typeCode}
                                    </a>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>

                    <%-- DAY TYPE TOGGLE --%>
                    <div class="col-md-4">
                        <div class="btn-group w-100 p-1 bg-light rounded" role="group">
                            <a href="?facilityId=${viewData.facilityId}&courtTypeId=${viewData.currentCourtTypeId}&dayType=WEEKDAY"
                               class="btn ${viewData.currentDayType == 'WEEKDAY' ? 'btn-primary' : 'btn-outline-secondary'}">
                                TRONG TUẦN(2-6)
                            </a>
                            <a href="?facilityId=${viewData.facilityId}&courtTypeId=${viewData.currentCourtTypeId}&dayType=WEEKEND"
                               class="btn ${viewData.currentDayType == 'WEEKEND' ? 'btn-primary' : 'btn-outline-secondary'}">
                                CUỐI TUẦN
                            </a>
                        </div>
                    </div>

                    <%-- ACTIONS --%>
                    <div class="col-md-3 text-end">
                        <button class="btn btn-accent" onclick="openCreateModal()">
                            <i class="bi bi-plus-circle me-1"></i> Thêm khoảng giá
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <%-- PRICING TABLE CONTAINER --%>
        <div class="card border-0 shadow-sm position-relative">
            <div class="card-body p-0">
                <div id="pricingTableContainer">
                    <%@ include file="pricing-table.jsp" %>
                </div>
            </div>
        </div>

        <%-- LOADING OVERLAY (Outside of card to prevent removal) --%>
        <div id="loadingOverlay" class="d-none position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center bg-white bg-opacity-75" style="z-index: 9999;">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
        </div>
    </div>

    <%@ include file="../layout/footer.jsp" %>
</div>

<%-- MODALS --%>
<%@ include file="price-rule-modals.jsp" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pricing.css">
<script>
    // Global state for JS
    window.pricingContext = {
        contextPath: '${pageContext.request.contextPath}',
        facilityId: ${viewData.facilityId},
        courtTypeId: ${viewData.currentCourtTypeId},
        dayType: '${viewData.currentDayType}'
    };
</script>
<script src="${pageContext.request.contextPath}/assets/js/pricing.js"></script>
