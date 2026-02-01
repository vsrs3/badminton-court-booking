<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="sidebar">
    <div class="sidebar-header">
        <a href="${pageContext.request.contextPath}/owner/dashboard" class="sidebar-brand">
            <i class="bi bi-circle-fill text-accent"></i>
            <span>BCB Admin</span>
        </a>
    </div>

    <div class="sidebar-nav">
        <a href="${pageContext.request.contextPath}/owner/dashboard"
           class="nav-link ${pageContext.request.requestURI.contains('/dashboard') ? 'active' : ''}">
            <i class="bi bi-speedometer2"></i>
            <span>Dashboard</span>
        </a>
        
        <a href="${pageContext.request.contextPath}/owner/facility/list"
           class="nav-link ${pageContext.request.requestURI.contains('/facility/list') ? 'active' : ''}">
            <i class="bi bi-building"></i>
            <span>My Locations</span>
        </a>

        <a href="#" class="nav-link">
            <i class="bi bi-gear"></i>
            <span>Settings</span>
        </a>
        
        <div class="mt-auto pt-4">
            <a href="${pageContext.request.contextPath}/logout" class="nav-link text-danger border-top border-secondary mt-3 pt-3">
                <i class="bi bi-box-arrow-right"></i>
                <span>Logout</span>
            </a>
        </div>
    </div>
</div>