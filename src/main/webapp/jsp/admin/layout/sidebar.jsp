<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="sidebar">
    <div class="sidebar-header">
        <h5><i class="bi bi-badminton"></i> BCB Admin</h5>
    </div>

    <nav class="sidebar-nav">
        <ul class="nav flex-column">
            <li class="nav-item">
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-link active">
                    <i class="bi bi-speedometer2"></i> <span>Dashboard</span>
                </a>
            </li>
            <li class="nav-item">
                <a href="${pageContext.request.contextPath}/admin/facility/list" class="nav-link">
                    <i class="bi bi-building"></i> <span>My Locations</span>
                </a>
            </li>
            <li class="nav-item">
                <a href="#" class="nav-link">
                    <i class="bi bi-gear"></i> <span>Settings</span>
                </a>
            </li>
            <li class="nav-item mt-auto border-top pt-3">
                <a href="${pageContext.request.contextPath}/logout" class="nav-link text-warning">
                    <i class="bi bi-box-arrow-right"></i> <span>Logout</span>
                </a>
            </li>
        </ul>
    </nav>
</div>