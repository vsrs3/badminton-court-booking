<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ include file="../layout/layout.jsp" %>
<%@ include file="../layout/sidebar.jsp" %>

<div class="main-content">

<%@ include file="../layout/header.jsp" %>

<div class="content-area">

<%@ include file="../layout/page-header.jsp" %>

<!-- ========================= -->
<!-- KHO ĐỒ CỦA SÂN -->
<!-- ========================= -->

<div class="card mb-4 shadow-sm border-0 rounded-4">
<div class="card-body p-4">

<h4 class="fw-bold text-emerald mb-3">
Kho đồ của sân
</h4>

<div class="table-responsive">
<table class="table table-hover">

<thead class="table-light">
<tr>
<th>ID</th>
<th>Tên</th>
<th>Thương hiệu</th>
<th>Giá thuê</th>
<th>Sân</th>
<th class="text-end">Hành động</th>
</tr>
</thead>

<tbody>

<c:choose>

<c:when test="${empty inventories}">
<tr>
<td colspan="6" class="text-center text-muted">
Không có dụng cụ
</td>
</tr>
</c:when>

<c:otherwise>

<c:forEach items="${inventories}" var="i">

<tr>

<td>${i.inventoryId}</td>
<td>${i.name}</td>
<td>${i.brand}</td>
<td>${i.rentalPrice}</td>

<td>
<span class="badge bg-success">
${i.facilityName}
</span>
</td>

<td class="text-end">

<form method="post"
action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}">

<input type="hidden" name="action" value="remove"/>
<input type="hidden" name="facilityId" value="${facilityId}"/>
<input type="hidden" name="inventoryId" value="${i.inventoryId}"/>

<button class="btn btn-sm btn-danger">
Xóa
</button>

</form>

</td>

</tr>

</c:forEach>

</c:otherwise>

</c:choose>

</tbody>

</table>
</div>

<!-- PAGINATION INVENTORY -->

<nav>
<ul class="pagination justify-content-center">

<c:forEach begin="1" end="${totalPages}" var="p">

<li class="page-item ${p==currentPage?'active':''}">

<a class="page-link"
href="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}?page=${p}">

${p}

</a>

</li>

</c:forEach>

</ul>
</nav>

</div>
</div>


<!-- ========================= -->
<!-- DỤNG CỤ CHƯA GÁN SÂN -->
<!-- ========================= -->

<div class="card shadow-sm border-0 rounded-4">
<div class="card-body p-4">

<h5 class="fw-bold mb-3">
Dụng cụ chưa gán sân
</h5>

<div class="table-responsive">

<table class="table table-hover">

<thead class="table-light">
<tr>
<th>Tên</th>
<th>Thương hiệu</th>
<th class="text-end">Gán vào sân</th>
</tr>
</thead>

<tbody>

<c:choose>

<c:when test="${empty unassigned}">
<tr>
<td colspan="3" class="text-center text-muted">
Không có dụng cụ trống
</td>
</tr>
</c:when>

<c:otherwise>

<c:forEach items="${unassigned}" var="i">

<tr>

<td>${i.name}</td>
<td>${i.brand}</td>

<td class="text-end">

<form method="post"
action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}">

<input type="hidden" name="action" value="assign"/>
<input type="hidden" name="facilityId" value="${facilityId}"/>
<input type="hidden" name="inventoryId" value="${i.inventoryId}"/>

<button class="btn btn-success btn-sm">

Gán vào sân

</button>

</form>

</td>

</tr>

</c:forEach>

</c:otherwise>

</c:choose>

</tbody>

</table>

</div>

<!-- PAGINATION UNASSIGNED -->

<nav>
<ul class="pagination justify-content-center">

<c:forEach begin="1" end="${totalPagesUn}" var="p">

<li class="page-item ${p==currentPageUn?'active':''}">

<a class="page-link"
href="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}?pageUn=${p}">

${p}

</a>

</li>

</c:forEach>

</ul>
</nav>

</div>
</div>

</div>

<%@ include file="../layout/footer.jsp" %>

</div>