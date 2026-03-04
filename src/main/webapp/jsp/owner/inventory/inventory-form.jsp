<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ include file="../layout/layout.jsp"%>
<%@ include file="../layout/sidebar.jsp"%>

<div class="main-content">

<%@ include file="../layout/header.jsp"%>

<div class="content-area">

<!-- PAGE HEADER -->

<div class="mb-4">

<h1 class="fw-black mb-1"
style="font-size:1.75rem;color:var(--color-gray-900);">

<c:choose>

<c:when test="${inventory != null}">
Edit Inventory
</c:when>

<c:otherwise>
Add New Inventory
</c:otherwise>

</c:choose>

</h1>

</div>


<div class="card border-0 rounded-4"
style="box-shadow:0 1px 6px rgba(0,0,0,0.08);">

<div class="card-body p-4">


<form method="post"
action="${pageContext.request.contextPath}/owner/inventory">


<c:if test="${inventory != null}">

<input type="hidden"
name="id"
value="${inventory.inventoryId}"/>

</c:if>


<!-- NAME -->

<div class="mb-3">

<label class="form-label fw-semibold">
Equipment Name
</label>

<input type="text"
name="name"
class="form-control rounded-3"
value="${inventory.name}"
required/>

</div>


<!-- BRAND -->

<div class="mb-3">

<label class="form-label fw-semibold">
Brand
</label>

<input type="text"
name="brand"
class="form-control rounded-3"
value="${inventory.brand}"
required/>

</div>


<!-- DESCRIPTION -->

<div class="mb-3">

<label class="form-label fw-semibold">
Description
</label>

<textarea name="description"
class="form-control rounded-3"
rows="4">

${inventory.description}

</textarea>

</div>


<!-- PRICE -->

<div class="mb-3">

<label class="form-label fw-semibold">
Rental Price
</label>

<input type="number"
step="0.01"
name="price"
class="form-control rounded-3"
value="${inventory.rentalPrice}"
required/>

</div>


<!-- FACILITY DROPDOWN -->

<div class="mb-3">

<label class="form-label fw-semibold">
Facility
</label>

<select name="facilityId"
class="form-select rounded-3">

<option value="">
-- Select Facility --
</option>

<c:forEach items="${facilities}" var="f">

<option value="${f.facilityId}"

<c:if test="${inventory != null && inventory.facilityId == f.facilityId}">
selected
</c:if>

>

${f.name}

</option>

</c:forEach>

</select>

</div>


<!-- ACTIVE -->

<div class="form-check mb-4">

<input class="form-check-input"
type="checkbox"
name="active"
id="activeCheck"

<c:if test="${inventory != null && inventory.active}">
checked
</c:if>

/>

<label class="form-check-label fw-semibold"
for="activeCheck">

Active

</label>

</div>


<!-- BUTTON -->

<div class="d-flex gap-3">

<button type="submit"
class="btn btn-success rounded-3 px-4">

Save

</button>

<a href="${pageContext.request.contextPath}/owner/inventory"
class="btn btn-outline-secondary rounded-3 px-4">

Cancel

</a>

</div>


</form>

</div>

</div>

</div>

<%@ include file="../layout/footer.jsp"%>

</div>