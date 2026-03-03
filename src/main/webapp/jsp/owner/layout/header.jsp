<!-- header.jsp -->

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<nav class="bg-white border-bottom sticky-top shadow-sm"
	style="height:64px;z-index:30;">
	<div class="d-flex align-items-center justify-content-between h-100 px-4">

		<%-- Brand label --%>
		<span class="d-none d-md-block text-uppercase fw-semibold text-secondary"
			style="font-size:0.75rem;letter-spacing:0.12em;">
			Trung tâm quản lý chủ sân
		</span>

		<%-- Right side --%>
		<div class="d-flex align-items-center gap-3 ms-auto">

			<%-- Notification bell --%>
			<div class="position-relative">
				<button class="btn btn-link text-secondary p-2 rounded-circle"
					style="line-height:1;"
					onmouseover="this.style.color='var(--color-green-brand)';this.style.background='var(--color-green-50)'"
					onmouseout="this.style.color='';this.style.background=''">
					<i class="bi bi-bell fs-5"></i>
				</button>
				<c:if test="${sessionScope.unreadNotifications > 0}">
					<span class="position-absolute badge rounded-pill bg-danger border border-white fw-bold"
						style="font-size:0.5625rem;top:4px;right:4px;padding:2px 5px;">
						${sessionScope.unreadNotifications}
					</span>
				</c:if>
			</div>

			<%-- Divider --%>
			<div class="border-start" style="height:32px;"></div>

			<%-- User info + Avatar --%>
			<div class="d-flex align-items-center gap-2">
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

		</div>
	</div>
</nav>
