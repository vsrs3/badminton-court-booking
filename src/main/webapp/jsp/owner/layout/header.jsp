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
        <c:choose>
            <c:when test="${not empty sessionScope.account.avatarPath}">
                <img
                        src="${pageContext.request.contextPath}/uploads/${sessionScope.account.avatarPath}"
                        alt="Avatar"
                        style="width: 36px; height: 36px; object-fit: cover;"
                        class="rounded-circle border"
                />
            </c:when>
            <c:otherwise>
                <div style="width: 36px; height: 36px;"
                     class="rounded-circle bg-secondary d-flex align-items-center justify-content-center text-white">
                    <i class="bi bi-person-fill"></i>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</nav>
