<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ include file="../layout/layout.jsp" %>
<%@ include file="../layout/sidebar.jsp" %>

<div class="main-content">

    <%-- HEADER CHUNG --%>
    <%@ include file="../layout/header.jsp" %>

    <div class="content-area">

        <%-- PAGE HEADER --%>
        <div class="mb-4">
            <h4 class="mb-1">
                ${court.courtName}
            </h4>
            <div class="text-muted">
                Facility: <strong>${facility.name}</strong>
            </div>
        </div>

        <%-- BACK BUTTON --%>
            <a href="${pageContext.request.contextPath}/owner/courts/list/${facility.facilityId}"
               class="btn btn-outline-secondary btn-sm">
                <i class="bi bi-arrow-left"></i> Back to Courts
            </a>


        <%-- TABS --%>
        <ul class="nav nav-tabs mb-3" role="tablist">
            <li class="nav-item">
                <button class="nav-link active"
                        data-bs-toggle="tab"
                        data-bs-target="#priceTab"
                        type="button">
                    <i class="bi bi-currency-dollar"></i> Price Configuration
                </button>
            </li>
            <li class="nav-item">
                <button class="nav-link"
                        data-bs-toggle="tab"
                        data-bs-target="#blockTab"
                        type="button">
                    <i class="bi bi-calendar-x"></i> Block Schedule
                </button>
            </li>
        </ul>

        <%-- TAB CONTENT --%>
        <div class="tab-content">

            <%-- TAB 1: PRICE CONFIGURATION --%>
            <div class="tab-pane fade show active" id="priceTab">
<%--                <%@ include file="price/court-price-list.jsp" %>--%>
            </div>

            <%-- TAB 2: BLOCK SCHEDULE --%>
            <div class="tab-pane fade" id="blockTab">
<%--                <%@ include file="court-block-list.jsp" %>--%>
            </div>

        </div>

    </div>

    <%@ include file="../layout/footer.jsp" %>
</div>
