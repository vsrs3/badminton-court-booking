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
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 class="h3 mb-1 text-emerald">Configure Rates</h2>
                <p class="text-muted mb-0">Set the hourly pricing for <strong>${viewData.facilityName}</strong></p>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/owner/facility/view/${viewData.facilityId}" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i> Return to Detail
                </a>
            </div>
        </div>

        <%-- ALERTS --%>
        <div id="alertContainer">
            <c:if test="${not empty param.error}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    <i class="bi bi-exclamation-circle me-2"></i> ${param.error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>
            <c:if test="${not empty param.message}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    <i class="bi bi-check-circle me-2"></i> ${param.message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
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
                                    <button class="nav-link ${viewData.currentCourtTypeId == type.courtTypeId ? 'active' : ''}" 
                                            data-type-id="${type.courtTypeId}"
                                            onclick="switchCourtType(${type.courtTypeId})">
                                        ${type.typeCode} COURTS
                                    </button>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>

                    <%-- DAY TYPE TOGGLE --%>
                    <div class="col-md-4">
                        <div class="btn-group w-100 p-1 bg-light rounded" role="group">
                            <input type="radio" class="btn-check" name="dayType" id="dayWeekday" value="WEEKDAY" 
                                   ${viewData.currentDayType == 'WEEKDAY' ? 'checked' : ''} onchange="switchDayType('WEEKDAY')">
                            <label class="btn btn-day-toggle" for="dayWeekday">WEEKDAY</label>

                            <input type="radio" class="btn-check" name="dayType" id="dayWeekend" value="WEEKEND" 
                                   ${viewData.currentDayType == 'WEEKEND' ? 'checked' : ''} onchange="switchDayType('WEEKEND')">
                            <label class="btn btn-day-toggle" for="dayWeekend">WEEKEND</label>
                        </div>
                    </div>

                    <%-- ACTIONS --%>
                    <div class="col-md-3 text-end">
                        <button class="btn btn-light border me-2" title="Copy Configuration" data-bs-toggle="modal" data-bs-target="#placeholderModal">
                            <i class="bi bi-copy"></i>
                        </button>
                        <button class="btn btn-accent" data-bs-toggle="modal" data-bs-target="#bulkUpdateModal">
                            <i class="bi bi-plus-circle me-1"></i> Bulk Update
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <%-- PRICING TABLE CONTAINER --%>
        <div class="card border-0 shadow-sm">
            <div class="card-body p-0">
                <div id="pricingTableContainer">
                    <%@ include file="pricing-table.jsp" %>
                </div>
                
                <%-- LOADING OVERLAY --%>
                <div id="loadingOverlay" class="d-none">
                    <div class="position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center bg-white bg-opacity-75" style="z-index: 10;">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">Loading...</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <%@ include file="../layout/footer.jsp" %>
</div>

<%-- MODALS --%>
<%@ include file="bulk-update-modal.jsp" %>

<%-- Placeholder Modal for Copy --%>
<div class="modal fade" id="placeholderModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Copy Configuration</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <p>This feature is coming soon.</p>
            </div>
        </div>
    </div>
</div>

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
