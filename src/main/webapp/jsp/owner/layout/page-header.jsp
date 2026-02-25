<!-- page-header.jsp -->

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<div class="d-flex justify-content-between align-items-center mb-3">
	<h4 class="fw-black mb-0" style="font-size:1.25rem;color:var(--color-gray-800);letter-spacing:-0.01em;">
		<c:out value="${pageTitle}" />
	</h4>

	<c:if test="${not empty sessionScope.fullName}">
		<span class="text-secondary" style="font-size:0.875rem;">
			Xin chào, <strong class="text-dark">${sessionScope.fullName}</strong>
		</span>
	</c:if>
</div>
<hr class="mt-0 mb-4" style="border-color:var(--color-gray-200);">
