<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ include file="../layout/layout.jsp"%>
<%@ include file="../layout/sidebar.jsp"%>

<div class="main-content">
    <%@ include file="../layout/header.jsp"%>

    <div class="content-area">

        <!-- PAGE HEADER -->
        <div class="d-flex align-items-center justify-content-between mb-4">
            <div>
                <h1 class="fw-black mb-1"
                    style="font-size:1.75rem;color:var(--color-gray-900);letter-spacing:-0.02em;">
                    Inventory Management
                </h1>
                <p class="text-secondary mb-0" style="font-size:0.875rem;">
                    Manage rental equipment
                </p>
            </div>

            <a href="${pageContext.request.contextPath}/owner/inventory?action=add"
               class="btn btn-brand d-flex align-items-center gap-2 fw-bold rounded-3"
               style="padding:0.625rem 1.25rem;">
                <i class="bi bi-plus-circle"></i> Add New
            </a>
        </div>

        <!-- SEARCH CARD -->
        <div class="card border-0 rounded-4 mb-4"
             style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
            <div class="card-body p-4">
                <form method="get"
                      action="${pageContext.request.contextPath}/owner/inventory"
                      class="row g-3">

                    <div class="col-md-4">
                        <input type="text"
                               name="keyword"
                               value="${keyword}"
                               class="form-control rounded-3"
                               placeholder="Search equipment..."/>
                    </div>

                    <div class="col-auto">
                        <button class="btn btn-outline-success rounded-3">
                            <i class="bi bi-search"></i> Search
                        </button>
                    </div>

                </form>
            </div>
        </div>

        <!-- TABLE CARD -->
        <div class="card border-0 rounded-4"
             style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
            <div class="card-body p-4">

                <div class="table-responsive">
                    <table class="table align-middle">
                        <thead class="table-light">
                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Brand</th>
                            <th>Price</th>
                            <th>Status</th>
                            <th>Court</th>
                            <th class="text-end">Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${inventories}" var="i">
                            <tr>
                                <td>${i.inventoryId}</td>
                                <td class="fw-semibold">${i.name}</td>
                                <td>${i.brand}</td>
                                <td>${i.rentalPrice}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${i.active}">
                                            <span class="badge bg-success">Active</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">Inactive</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
    <c:if test="${not empty i.courtName}">
        ${i.courtName}
    </c:if>
</td>
                                <td class="text-end">
                                    <a href="${pageContext.request.contextPath}/owner/inventory?action=edit&id=${i.inventoryId}"
                                       class="btn btn-sm btn-outline-primary">
                                        <i class="bi bi-pencil"></i>
                                    </a>

                                    <a href="${pageContext.request.contextPath}/owner/inventory?action=delete&id=${i.inventoryId}"
                                       onclick="return confirm('Delete this item?')"
                                       class="btn btn-sm btn-outline-danger">
                                        <i class="bi bi-trash"></i>
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>

                        <c:if test="${empty inventories}">
                            <tr>
                                <td colspan="7" class="text-center text-muted">
                                    No inventory found.
                                </td>
                            </tr>
                        </c:if>

                        </tbody>
                    </table>
                </div>

            </div>
        </div>

    </div>

    <%@ include file="../layout/footer.jsp"%>
</div>