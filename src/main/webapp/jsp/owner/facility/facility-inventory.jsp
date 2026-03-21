<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ include file="../layout/layout.jsp" %>
<%@ include file="../layout/sidebar.jsp" %>
<div class="main-content">
<%@ include file="../layout/header.jsp" %>
<div class="content-area">
<%@ include file="../layout/page-header.jsp" %>
<div class="card shadow-sm border-0 rounded-4"><div class="card-body p-4">
<c:if test="${not empty sessionScope.successMessage}"><div class="alert alert-success alert-dismissible fade show rounded-3" role="alert">${sessionScope.successMessage}<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button></div><c:remove var="successMessage" scope="session"/></c:if>
<c:if test="${not empty sessionScope.errorMessage}"><div class="alert alert-danger alert-dismissible fade show rounded-3" role="alert">${sessionScope.errorMessage}<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button></div><c:remove var="errorMessage" scope="session"/></c:if>

<div class ="mb-5">
<div class ="d-flex justify-content-between align-items-center mb-3"><div><h5 class="fw-bold text-dark mb-1">Đồ gán sân</h5><p class="text-muted mb-0">Danh sách sản phẩm đã được gán cho sân hiện tại.</p></div></div>
<form method ="get" action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}" class="row g-3 align-items-center mb-4 inventory-search-form">
<div class ="col-lg-5 col-md-7 inventory-search-input">
<div class ="search-suggestion-wrap">
<input type ="text" id="assignedKeywordInput" name="assignedKeyword" value="${assignedKeyword}" class="form-control rounded-3" placeholder="Tìm theo tên sản phẩm đã gán" aria-label="Tìm theo tên sản phẩm đã gán" autocomplete="off">
<div id="assignedSuggestionMenu" class="search-suggestion-menu"></div>
</div>
<div class="form-text">Gợi ý lấy trong ${suggestionLimit} dữ liệu đầu tiên. Bấm tìm kiếm để tra toàn bộ dữ liệu.</div>
</div>
<input type="hidden" name="facilityId" value="${facilityId}"/>
<input type="hidden" name="inventoryKeyword" value="${inventoryKeyword}"/>
<input type="hidden" name="assignedPage" id="assignedPageField" value="${assignedCurrentPage}"/>
<input type="hidden" name="inventoryPage" value="${inventoryCurrentPage}"/>
<div class="col-auto d-flex flex-wrap gap-2">
<button type="submit" id="assignedSearchBtn" class="btn btn-outline-success rounded-3">Tìm kiếm</button>
<button type="submit" formmethod="post" formaction="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}" name="action" value="removeAll" class="btn btn-outline-danger rounded-3" onclick="return confirm('Bạn có chắc muốn gỡ tất cả sản phẩm đang gán khỏi sân này không?');">Gỡ tất cả</button>
<button type="button" id="toggleBulkQuantityBtn" class="btn btn-success rounded-3">Thêm hàng loạt</button>
</div>
</form>
<div id="bulkQuantityPanel" class="card border-0 bg-light rounded-4 mb-4 d-none"><div class="card-body">
<form method="post" action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}" class="row g-3 align-items-end">
<input type="hidden" name="action" value="bulkUpdateQuantity"/><input type="hidden" name="facilityId" value="${facilityId}"/><input type="hidden" name="assignedKeyword" value="${assignedKeyword}"/><input type="hidden" name="inventoryKeyword" value="${inventoryKeyword}"/><input type="hidden" name="assignedPage" value="${assignedCurrentPage}"/><input type="hidden" name="inventoryPage" value="${inventoryCurrentPage}"/>
<div class="col-lg-4 col-md-6"><label for="bulkQuantity" class="form-label fw-semibold">Số lượng áp dụng</label><input type="number" id="bulkQuantity" name="bulkQuantity" min="0" step="1" class="form-control rounded-3" data-non-negative="true" data-quantity-label="Số lượng áp dụng" required></div>
<div class="col-12 d-flex flex-wrap gap-2"><button type="submit" class="btn btn-success rounded-3">Lưu</button><button type="button" id="cancelBulkQuantityBtn" class="btn btn-outline-secondary rounded-3">Hủy</button></div>
</form>
</div></div>
<div class="table-responsive"><table class="table table-hover align-middle">
<thead class="table-light"><tr><th>STT</th><th>Tên sân</th><th>Tên sản phẩm</th><th>Số lượng sản phẩm</th><th>Số lượng khả dụng</th><th class="text-center">Thao tác</th></tr></thead>
<tbody>
<c:choose>
<c:when test="${empty assignedItems}"><tr><td colspan="6" class="text-center text-muted">Chưa có sản phẩm nào được gán cho sân.</td></tr></c:when>
<c:otherwise>
<c:forEach items="${assignedItems}" var="item" varStatus="status">
<tr>
<td>${(assignedCurrentPage - 1) * assignedPageSize + status.index + 1}</td><td>${item.facilityName}</td><td>${item.inventoryName}</td>
<td style="width:190px;"><input type="number" name="totalQuantity" min="0" step="1" value="${item.totalQuantity}" class="form-control form-control-sm rounded-3" form="updateForm_${item.facilityInventoryId}" data-non-negative="true" data-quantity-label="Số lượng sản phẩm" aria-label="Số lượng sản phẩm" required></td>
<td style="width:190px;"><input type="number" value="${item.availableQuantity}" class="form-control form-control-sm rounded-3" readonly aria-label="Số lượng khả dụng"></td>
<td class="text-center" style="min-width:220px;"><div class="d-flex justify-content-center gap-2">
<form id="updateForm_${item.facilityInventoryId}" method="post" action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}"><input type="hidden" name="action" value="updateQuantity"/><input type="hidden" name="facilityId" value="${facilityId}"/><input type="hidden" name="facilityInventoryId" value="${item.facilityInventoryId}"/><input type="hidden" name="assignedKeyword" value="${assignedKeyword}"/><input type="hidden" name="inventoryKeyword" value="${inventoryKeyword}"/><input type="hidden" name="assignedPage" value="${assignedCurrentPage}"/><input type="hidden" name="inventoryPage" value="${inventoryCurrentPage}"/></form>
<button type="submit" form="updateForm_${item.facilityInventoryId}" class="btn btn-sm btn-success rounded-3">Lưu</button>
<form method="post" action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}" onsubmit="return confirm('Bạn có chắc muốn gỡ sản phẩm này khỏi sân không?');"><input type="hidden" name="action" value="remove"/><input type="hidden" name="facilityId" value="${facilityId}"/><input type="hidden" name="facilityInventoryId" value="${item.facilityInventoryId}"/><input type="hidden" name="assignedKeyword" value="${assignedKeyword}"/><input type="hidden" name="inventoryKeyword" value="${inventoryKeyword}"/><input type="hidden" name="assignedPage" value="${assignedCurrentPage}"/><input type="hidden" name="inventoryPage" value="${inventoryCurrentPage}"/><button type="submit" class="btn btn-sm btn-outline-danger rounded-3">Gỡ</button></form>
</div></td></tr>
</c:forEach>
</c:otherwise>
</c:choose>
</tbody></table></div>
<c:if test="${assignedTotalPages > 1}"><nav class="mt-4"><ul class="pagination justify-content-center align-items-center gap-2 compact-pagination">
<li class="page-item ${assignedCurrentPage == 1 ? 'disabled' : ''}"><c:choose><c:when test="${assignedCurrentPage == 1}"><span class="page-link-static" aria-label="Trang trước"><i class="bi bi-chevron-left"></i></span></c:when><c:otherwise><a class="page-link" aria-label="Trang trước" href="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}?assignedPage=${assignedCurrentPage - 1}&assignedKeyword=${assignedKeyword}&inventoryKeyword=${inventoryKeyword}&inventoryPage=${inventoryCurrentPage}"><i class="bi bi-chevron-left"></i></a></c:otherwise></c:choose></li>
<li class="page-item active"><span class="page-link-static">${assignedCurrentPage}</span></li>
<c:if test="${assignedCurrentPage + 1 < assignedTotalPages}"><li class="page-item disabled"><span class="page-link-static pagination-ellipsis">...</span></li></c:if>
<c:if test="${assignedCurrentPage < assignedTotalPages}"><li class="page-item"><a class="page-link" href="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}?assignedPage=${assignedTotalPages}&assignedKeyword=${assignedKeyword}&inventoryKeyword=${inventoryKeyword}&inventoryPage=${inventoryCurrentPage}">${assignedTotalPages}</a></li></c:if>
<li class="page-item ${assignedCurrentPage == assignedTotalPages ? 'disabled' : ''}"><c:choose><c:when test="${assignedCurrentPage == assignedTotalPages}"><span class="page-link-static" aria-label="Trang sau"><i class="bi bi-chevron-right"></i></span></c:when><c:otherwise><a class="page-link" aria-label="Trang sau" href="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}?assignedPage=${assignedCurrentPage + 1}&assignedKeyword=${assignedKeyword}&inventoryKeyword=${inventoryKeyword}&inventoryPage=${inventoryCurrentPage}"><i class="bi bi-chevron-right"></i></a></c:otherwise></c:choose></li>
</ul></nav></c:if>
</div>

<div>
<div class="d-flex justify-content-between align-items-center mb-3"><div><h5 class="fw-bold text-dark mb-1">Kho đồ tổng</h5><p class="text-muted mb-0">Danh sách sản phẩm đang hoạt động và chưa gán cho sân hiện tại.</p></div></div>
<form method="get" action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}" class="row g-3 align-items-center mb-4 inventory-search-form">
<div class="col-lg-5 col-md-7 inventory-search-input">
<div class="search-suggestion-wrap">
<input type="text" id="inventoryKeywordInput" name="inventoryKeyword" value="${inventoryKeyword}" class="form-control rounded-3" placeholder="Tìm trong kho đồ tổng" aria-label="Tìm trong kho đồ tổng" autocomplete="off">
<div id="inventorySuggestionMenu" class="search-suggestion-menu"></div>
</div>
<div class="form-text">Gợi ý lấy trong ${suggestionLimit} dữ liệu đầu tiên. Bấm tìm kiếm để tra toàn bộ dữ liệu.</div>
</div>
<input type="hidden" name="facilityId" value="${facilityId}"/><input type="hidden" name="assignedKeyword" value="${assignedKeyword}"/><input type="hidden" name="assignedPage" value="${assignedCurrentPage}"/><input type="hidden" name="inventoryPage" id="inventoryPageField" value="${inventoryCurrentPage}"/>
<div class="col-auto d-flex flex-wrap gap-2"><button type="submit" id="inventorySearchBtn" class="btn btn-outline-success rounded-3">Tìm kiếm</button><button type="submit" formmethod="post" formaction="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}" name="action" value="assignAll" class="btn btn-success rounded-3">Gán tất cả</button></div>
</form>
<div class="table-responsive"><table class="table table-hover align-middle">
<thead class="table-light"><tr><th>STT</th><th>Tên</th><th>Thương hiệu</th><th>Mô tả</th><th>Giá thuê</th><th>Trạng thái</th><th class="text-center">Thao tác</th></tr></thead>
<tbody>
<c:choose>
<c:when test="${empty inventories}"><tr><td colspan="7" class="text-center text-muted">Không còn sản phẩm khả dụng để gán cho sân.</td></tr></c:when>
<c:otherwise>
<c:forEach items="${inventories}" var="i" varStatus="status">
<tr>
<td>${(inventoryCurrentPage - 1) * inventoryPageSize + status.index + 1}</td><td>${i.name}</td><td>${i.brand}</td><td>${i.description}</td><td>${i.rentalPrice}</td><td><span class="badge bg-success">Đang hoạt động</span></td>
<td class="text-center"><form method="post" action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}"><input type="hidden" name="action" value="assign"/><input type="hidden" name="facilityId" value="${facilityId}"/><input type="hidden" name="inventoryId" value="${i.inventoryId}"/><input type="hidden" name="assignedKeyword" value="${assignedKeyword}"/><input type="hidden" name="inventoryKeyword" value="${inventoryKeyword}"/><input type="hidden" name="assignedPage" value="${assignedCurrentPage}"/><input type="hidden" name="inventoryPage" value="${inventoryCurrentPage}"/><button type="submit" class="btn btn-sm btn-primary rounded-3">Gán</button></form></td>
</tr>
</c:forEach>
</c:otherwise>
</c:choose>
</tbody></table></div>
<c:if test="${inventoryTotalPages > 1}"><nav class="mt-4"><ul class="pagination justify-content-center align-items-center gap-2 compact-pagination">
<li class="page-item ${inventoryCurrentPage == 1 ? 'disabled' : ''}"><c:choose><c:when test="${inventoryCurrentPage == 1}"><span class="page-link-static" aria-label="Trang trước"><i class="bi bi-chevron-left"></i></span></c:when><c:otherwise><a class="page-link" aria-label="Trang trước" href="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}?inventoryPage=${inventoryCurrentPage - 1}&inventoryKeyword=${inventoryKeyword}&assignedKeyword=${assignedKeyword}&assignedPage=${assignedCurrentPage}"><i class="bi bi-chevron-left"></i></a></c:otherwise></c:choose></li>
<li class="page-item active"><span class="page-link-static">${inventoryCurrentPage}</span></li>
<c:if test="${inventoryCurrentPage + 1 < inventoryTotalPages}"><li class="page-item disabled"><span class="page-link-static pagination-ellipsis">...</span></li></c:if>
<c:if test="${inventoryCurrentPage < inventoryTotalPages}"><li class="page-item"><a class="page-link" href="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}?inventoryPage=${inventoryTotalPages}&inventoryKeyword=${inventoryKeyword}&assignedKeyword=${assignedKeyword}&assignedPage=${assignedCurrentPage}">${inventoryTotalPages}</a></li></c:if>
<li class="page-item ${inventoryCurrentPage == inventoryTotalPages ? 'disabled' : ''}"><c:choose><c:when test="${inventoryCurrentPage == inventoryTotalPages}"><span class="page-link-static" aria-label="Trang sau"><i class="bi bi-chevron-right"></i></span></c:when><c:otherwise><a class="page-link" aria-label="Trang sau" href="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}?inventoryPage=${inventoryCurrentPage + 1}&inventoryKeyword=${inventoryKeyword}&assignedKeyword=${assignedKeyword}&assignedPage=${assignedCurrentPage}"><i class="bi bi-chevron-right"></i></a></c:otherwise></c:choose></li>
</ul></nav></c:if>
</div>

<div id="assignedSuggestionDataset" class="d-none"><c:forEach items="${assignedSuggestionItems}" var="item"><div class="suggestion-source-item"><span class="suggestion-source-name"><c:out value="${item.inventoryName}"/></span><span class="suggestion-source-label"><c:out value="${item.inventoryName}"/></span></div></c:forEach></div>
<div id="inventorySuggestionDataset" class="d-none"><c:forEach items="${inventorySuggestionItems}" var="item"><div class="suggestion-source-item"><span class="suggestion-source-name"><c:out value="${item.name}"/></span><span class="suggestion-source-label"><c:out value="${item.name}"/></span><span class="suggestion-source-meta"><c:out value="${item.brand}"/></span></div></c:forEach></div>

</div></div></div>
<%@ include file="../layout/footer.jsp" %>
</div>
<script>
document.addEventListener("DOMContentLoaded",function(){
const bulkPanel=document.getElementById("bulkQuantityPanel"),toggleBulkButton=document.getElementById("toggleBulkQuantityBtn"),cancelBulkButton=document.getElementById("cancelBulkQuantityBtn"),bulkQuantityInput=document.getElementById("bulkQuantity"),assignedSearchButton=document.getElementById("assignedSearchBtn"),assignedPageField=document.getElementById("assignedPageField"),inventorySearchButton=document.getElementById("inventorySearchBtn"),inventoryPageField=document.getElementById("inventoryPageField"),assignedKeywordInput=document.getElementById("assignedKeywordInput"),inventoryKeywordInput=document.getElementById("inventoryKeywordInput"),assignedSuggestionMenu=document.getElementById("assignedSuggestionMenu"),inventorySuggestionMenu=document.getElementById("inventorySuggestionMenu");
function normalizeSearchText(v){return(v||"").normalize("NFD").replace(/[\u0300-\u036f]/g,"").toLowerCase().trim();}
function collectSuggestionItems(datasetId){const uniqueMap=new Map(),nodes=document.querySelectorAll("#"+datasetId+" .suggestion-source-item");nodes.forEach(function(node){const name=(node.querySelector(".suggestion-source-name")?.textContent||"").trim(),label=(node.querySelector(".suggestion-source-label")?.textContent||name).trim(),meta=(node.querySelector(".suggestion-source-meta")?.textContent||"").trim(),key=normalizeSearchText(name+"|"+meta);if(!name||uniqueMap.has(key)){return;}uniqueMap.set(key,{name:name,label:label,meta:meta});});return Array.from(uniqueMap.values());}
function hideSuggestionMenu(menu){if(!menu){return;}menu.classList.remove("is-visible");menu.innerHTML="";}
function renderSuggestionMenu(menu,input,items){if(!menu||!input){return;}menu.innerHTML="";if(!items.length){const emptyState=document.createElement("div");emptyState.className="search-suggestion-empty";emptyState.textContent="Không có gợi ý trong 50 dữ liệu đầu tiên. Bấm Tìm kiếm để tra toàn bộ.";menu.appendChild(emptyState);menu.classList.add("is-visible");return;}items.forEach(function(item){const button=document.createElement("button"),title=document.createElement("span");button.type="button";button.className="search-suggestion-item";title.className="search-suggestion-title";title.textContent=item.label;button.appendChild(title);if(item.meta){const meta=document.createElement("span");meta.className="search-suggestion-meta";meta.textContent=item.meta;button.appendChild(meta);}button.addEventListener("mousedown",function(event){event.preventDefault();input.value=item.name;hideSuggestionMenu(menu);input.focus();});menu.appendChild(button);});menu.classList.add("is-visible");}
function setupSuggestionAutocomplete(input,menu,datasetId){if(!input||!menu){return;}const dataset=collectSuggestionItems(datasetId);function refreshSuggestions(){const keyword=normalizeSearchText(input.value);if(!keyword){hideSuggestionMenu(menu);return;}renderSuggestionMenu(menu,input,dataset.filter(function(item){return normalizeSearchText(item.label+" "+item.meta+" "+item.name).includes(keyword);}));}input.addEventListener("input",refreshSuggestions);input.addEventListener("focus",refreshSuggestions);document.addEventListener("click",function(event){if(event.target===input||menu.contains(event.target)){return;}hideSuggestionMenu(menu);});}
if(toggleBulkButton&&bulkPanel){toggleBulkButton.addEventListener("click",function(){bulkPanel.classList.remove("d-none");if(bulkQuantityInput){bulkQuantityInput.focus();}});}
if(cancelBulkButton&&bulkPanel){cancelBulkButton.addEventListener("click",function(){bulkPanel.classList.add("d-none");if(bulkQuantityInput){bulkQuantityInput.value="";bulkQuantityInput.setCustomValidity("");}});}
if(assignedSearchButton&&assignedPageField){assignedSearchButton.addEventListener("click",function(){assignedPageField.value="1";});}
if(inventorySearchButton&&inventoryPageField){inventorySearchButton.addEventListener("click",function(){inventoryPageField.value="1";});}
setupSuggestionAutocomplete(assignedKeywordInput,assignedSuggestionMenu,"assignedSuggestionDataset");
setupSuggestionAutocomplete(inventoryKeywordInput,inventorySuggestionMenu,"inventorySuggestionDataset");
const quantityInputs=document.querySelectorAll("input[data-non-negative='true']"),blockedKeys=["-","+","e","E"];
quantityInputs.forEach(function(input){input.addEventListener("keydown",function(event){if(blockedKeys.includes(event.key)){event.preventDefault();}});input.addEventListener("input",function(){this.setCustomValidity("");if(this.value!==""&&Number(this.value)<0){this.value="0";}});input.addEventListener("invalid",function(){const label=this.dataset.quantityLabel||"Giá trị";if(this.validity.valueMissing){this.setCustomValidity(label+" không được để trống.");return;}if(this.validity.rangeUnderflow){this.setCustomValidity(label+" phải lớn hơn hoặc bằng 0.");return;}if(this.validity.badInput||this.validity.stepMismatch){this.setCustomValidity(label+" phải là số nguyên không âm.");return;}this.setCustomValidity(label+" không hợp lệ.");});});
});
</script>
<script>
document.addEventListener("DOMContentLoaded",function(){
const bulkPanel=document.getElementById("bulkQuantityPanel"),toggleBulkButton=document.getElementById("toggleBulkQuantityBtn"),cancelBulkButton=document.getElementById("cancelBulkQuantityBtn"),bulkQuantityInput=document.getElementById("bulkQuantity"),assignedSearchButton=document.getElementById("assignedSearchBtn"),assignedPageField=document.getElementById("assignedPageField"),inventorySearchButton=document.getElementById("inventorySearchBtn"),inventoryPageField=document.getElementById("inventoryPageField"),assignedKeywordInput=document.getElementById("assignedKeywordInput"),inventoryKeywordInput=document.getElementById("inventoryKeywordInput"),assignedSuggestionMenu=document.getElementById("assignedSuggestionMenu"),inventorySuggestionMenu=document.getElementById("inventorySuggestionMenu");
function n(v){return(v||"").normalize("NFD").replace(/[\u0300-\u036f]/g,"").toLowerCase().trim();}
function items(id){const map=new Map();document.querySelectorAll("#"+id+" .suggestion-source-item").forEach(function(node){const name=(node.querySelector(".suggestion-source-name")?.textContent||"").trim(),label=(node.querySelector(".suggestion-source-label")?.textContent||name).trim(),meta=(node.querySelector(".suggestion-source-meta")?.textContent||"").trim(),key=n(name+"|"+meta);if(name&&!map.has(key)){map.set(key,{name:name,label:label,meta:meta});}});return Array.from(map.values());}
function hide(menu){if(menu){menu.classList.remove("is-visible");menu.innerHTML="";}}
function render(menu,input,list){if(!menu||!input){return;}menu.innerHTML="";if(!list.length){const empty=document.createElement("div");empty.className="search-suggestion-empty";empty.textContent="Khong co goi y trong 50 du lieu dau tien. Bam Tim kiem de tra toan bo.";menu.appendChild(empty);menu.classList.add("is-visible");return;}list.forEach(function(item){const b=document.createElement("button"),t=document.createElement("span");b.type="button";b.className="search-suggestion-item";t.className="search-suggestion-title";t.textContent=item.label;b.appendChild(t);if(item.meta){const m=document.createElement("span");m.className="search-suggestion-meta";m.textContent=item.meta;b.appendChild(m);}b.addEventListener("mousedown",function(e){e.preventDefault();input.value=item.name;hide(menu);input.focus();});menu.appendChild(b);});menu.classList.add("is-visible");}
function auto(input,menu,id){if(!input||!menu){return;}const data=items(id);function refresh(){const key=n(input.value);if(!key){hide(menu);return;}render(menu,input,data.filter(function(item){return n(item.label+" "+item.meta+" "+item.name).includes(key);}));}input.addEventListener("input",refresh);input.addEventListener("focus",refresh);document.addEventListener("click",function(e){if(e.target===input||menu.contains(e.target)){return;}hide(menu);});}
if(toggleBulkButton&&bulkPanel){toggleBulkButton.onclick=function(){bulkPanel.classList.remove("d-none");if(bulkQuantityInput){bulkQuantityInput.focus();}};}
if(cancelBulkButton&&bulkPanel){cancelBulkButton.onclick=function(){bulkPanel.classList.add("d-none");if(bulkQuantityInput){bulkQuantityInput.value="";bulkQuantityInput.setCustomValidity("");}};}
if(assignedSearchButton&&assignedPageField){assignedSearchButton.onclick=function(){assignedPageField.value="1";};}
if(inventorySearchButton&&inventoryPageField){inventorySearchButton.onclick=function(){inventoryPageField.value="1";};}
auto(assignedKeywordInput,assignedSuggestionMenu,"assignedSuggestionDataset");auto(inventoryKeywordInput,inventorySuggestionMenu,"inventorySuggestionDataset");
document.querySelectorAll("input[data-non-negative='true']").forEach(function(input){input.addEventListener("keydown",function(e){if(["-","+","e","E"].includes(e.key)){e.preventDefault();}});input.addEventListener("input",function(){this.setCustomValidity("");if(this.value!==""&&Number(this.value)<0){this.value="0";}});input.addEventListener("invalid",function(){const label=this.dataset.quantityLabel||"Gia tri";if(this.validity.valueMissing){this.setCustomValidity(label+" khong duoc de trong.");return;}if(this.validity.rangeUnderflow){this.setCustomValidity(label+" phai lon hon hoac bang 0.");return;}if(this.validity.badInput||this.validity.stepMismatch){this.setCustomValidity(label+" phai la so nguyen khong am.");return;}this.setCustomValidity(label+" khong hop le.");});});
});
</script>
