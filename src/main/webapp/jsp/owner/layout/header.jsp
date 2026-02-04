<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<nav class="navbar bg-white border-bottom px-4 header-top">
    <div class="ms-auto d-flex align-items-center gap-3">
        <div class="fw-bold">Welcome</div>

        <!-- Notification -->
        <div class="position-relative">
            <i class="bi bi-bell fs-5 text-muted"></i>
            <c:if test="${sessionScope.unreadNotifications > 0}">
                <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
                        ${sessionScope.unreadNotifications}
                </span>
            </c:if>
        </div>

        <!-- Avatar -->
        <img href="" src="${pageContext.request.contextPath}/uploads/${sessionScope.account.avatarPath}" alt="Avatar" class="w-14 h-14 rounded-full border-2 border-white/30" />
    </div>
</nav>
