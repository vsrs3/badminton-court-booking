<%-- staff-header.jsp --%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<nav class="staff-header-bar">
    <div class="d-flex align-items-center justify-content-between w-100">

        <%-- Left side: Mobile toggle + Page context --%>
        <div class="d-flex align-items-center gap-3">

            <%-- Hamburger for mobile --%>
            <button class="st-sidebar-toggle" onclick="openSidebar()"
                    aria-label="Mở menu">
                <i class="bi bi-list"></i>
            </button>

            <%-- Brand label (desktop) --%>
            <span class="d-none d-md-block text-uppercase fw-semibold"
                  style="font-size:0.75rem; letter-spacing:0.12em; color:var(--text-muted, #64748b);">
                Quản lý đặt sân
            </span>
        </div>

        <%-- Right side: Staff info --%>
        <div class="d-flex align-items-center gap-3">

            <%-- Facility badge --%>
            <c:if test="${not empty sessionScope.facilityName}">
                <div class="d-none d-lg-flex align-items-center gap-2 px-3 py-1 rounded-pill"
                     style="background:var(--color-green-50, #F0FDF4);
                            border:1px solid #D1FAE5;">
                    <i class="bi bi-building" style="font-size:0.875rem; color:#16A34A;"></i>
                    <span style="font-size:0.75rem; font-weight:700; color:#065F46;
                                 max-width:180px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">
                        <c:out value="${sessionScope.facilityName}"/>
                    </span>
                </div>
            </c:if>

            <%-- Divider --%>
            <div class="border-start d-none d-sm-block" style="height:32px;"></div>

            <%-- Staff avatar + name --%>
            <div class="d-flex align-items-center gap-2">
                <c:choose>
                    <c:when test="${not empty sessionScope.account.avatarPath}">
                        <img src="${pageContext.request.contextPath}/uploads/${sessionScope.account.avatarPath}"
                             alt="Avatar"
                             style="width:36px; height:36px; object-fit:cover;"
                             class="rounded-circle border"/>
                    </c:when>
                    <c:otherwise>
                        <div style="width:36px; height:36px;"
                             class="rounded-circle d-flex align-items-center justify-content-center text-white"
                             style="background:linear-gradient(135deg, #064E3B 0%, #065F46 100%);">
                            <i class="bi bi-person-fill"></i>
                        </div>
                    </c:otherwise>
                </c:choose>

                <%-- Staff name (desktop only) --%>
                <div class="d-none d-md-block">
                    <p class="mb-0 fw-bold" style="font-size:0.8125rem; color:#1F2937; line-height:1.2;">
                        <c:out value="${sessionScope.account.fullName}" default="Staff"/>
                    </p>
                    <p class="mb-0" style="font-size:0.625rem; font-weight:700; color:#9CA3AF;
                                          text-transform:uppercase; letter-spacing:0.08em;">
                        Nhân viên
                    </p>
                </div>
            </div>
        </div>

    </div>
</nav>