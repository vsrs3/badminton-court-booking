<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ include file="../layout/layout.jsp"%>
<%@ include file="../layout/sidebar.jsp"%>

<div class="main-content">

    <%@ include file="../layout/header.jsp"%>

    <div class="content-area">

        <div class="d-flex align-items-center justify-content-between mb-4">

            <div>
                <h1 class="fw-black mb-1"
                    style="font-size:1.75rem;color:var(--color-gray-900);">

                    Inventory Management

                </h1>

                <p class="text-secondary mb-0">
                    Manage rental equipment
                </p>
            </div>

            <a href="${pageContext.request.contextPath}/owner/inventory?action=add"
               class="btn btn-success rounded-3 px-4">

                <i class="bi bi-plus-circle"></i>
                Add Equipment

            </a>

        </div>


        <!-- SEARCH -->

        <div class="card border-0 rounded-4 mb-4"
             style="box-shadow:0 2px 8px rgba(0,0,0,0.06);">

            <div class="card-body">

                <form method="get"
                      action="${pageContext.request.contextPath}/owner/inventory"
                      class="row g-3">

                    <div class="col-md-4">

                        <input type="text"
                               name="keyword"
                               value="${keyword}"
                               class="form-control rounded-3"
                               placeholder="Search equipment...">

                    </div>

                    <div class="col-auto">

                        <button class="btn btn-outline-success rounded-3">

                            <i class="bi bi-search"></i>
                            Search

                        </button>

                    </div>

                </form>

            </div>

        </div>


        <!-- TABLE -->

        <div class="card border-0 rounded-4"
             style="box-shadow:0 2px 8px rgba(0,0,0,0.06);">

            <div class="card-body">

                <div class="table-responsive">

                    <table class="table align-middle">

                        <thead class="table-light">

                        <tr>
                            <th>STT</th>
                            <th>Name</th>
                            <th>Brand</th>
                            <th>Price</th>
                            <th>Status</th>
                            <th class="text-end">Action</th>
                        </tr>

                        </thead>

                        <tbody>

                        <c:forEach items="${inventories}" var="i" varStatus="loop">

                            <tr>

                                <td>${(currentPage - 1) * 10 + loop.count}</td>

                                <td class="fw-semibold">${i.name}</td>

                                <td>${i.brand}</td>

                                <td class="text-success fw-bold">
                                        ${i.rentalPrice}
                                </td>

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

                                <td colspan="6"
                                    class="text-center text-muted">

                                    No inventory found

                                </td>

                            </tr>

                        </c:if>

                        </tbody>

                    </table>

                </div>


                <!-- PAGINATION -->

                <nav class="mt-4">

                    <ul class="pagination justify-content-center">

                        <c:forEach begin="1" end="${totalPages}" var="p">

                            <li class="page-item ${p==currentPage?'active':''}">

                                <a class="page-link"
                                   href="${pageContext.request.contextPath}/owner/inventory?page=${p}&keyword=${keyword}">

                                        ${p}

                                </a>

                            </li>

                        </c:forEach>

                    </ul>

                </nav>


            </div>

        </div>

    </div>

    <%@ include file="../layout/footer.jsp"%>

</div>