<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="d-flex justify-content-between align-items-center mb-4">
    <h4 class="mb-0 fw-semibold">
        <c:out value="${pageTitle}" />
    </h4>

    <c:if test="${not empty sessionScope.fullName}">
        <div class="text-muted small">
            Welcome, <strong>${sessionScope.fullName}</strong>
        </div>
    </c:if>
</div>

<hr class="mt-0">
