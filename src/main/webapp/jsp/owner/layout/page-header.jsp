<!-- page-header.jsp -->

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%-- Breadcrumb Navigation --%>
<c:if test="${not empty breadcrumbItems}">
    <nav aria-label="breadcrumb">
        <ol class="breadcrumb mb-2">
            <c:forEach items="${breadcrumbItems}" var="item" varStatus="status">
                <c:choose>
                    <c:when test="${item.active}">
                        <li class="breadcrumb-item active" aria-current="page">
                            <c:out value="${item.label}" />
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="breadcrumb-item">
                            <a href="${item.url}"><c:out value="${item.label}" /></a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </ol>
    </nav>
</c:if>

<%-- Page Title --%>
<div class="d-flex justify-content-between align-items-center mb-4">
    <h4 class="mb-0 fw-semibold">
        <c:out value="${pageTitle}" />
    </h4>
</div>
<hr class="mt-0 mb-4" style="border-color:var(--color-gray-200);">
