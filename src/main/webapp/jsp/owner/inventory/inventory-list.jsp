<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ include file="../layout/layout.jsp"%>
<%@ include file="../layout/sidebar.jsp"%>

<div class="main-content">

    <%@ include file="../layout/header.jsp"%>

    <div class="content-area">

        <div class="d-flex align-items-center justify-content-between mb-4">
            <div>
                <h1 class="fw-black mb-1" style="font-size: 1.75rem; color: var(--color-gray-900);">
                    Quản lý dụng cụ
                </h1>
                <p class="text-secondary mb-0">
                    Quản lý danh sách dụng cụ cho thuê
                </p>
            </div>

            <a href="${pageContext.request.contextPath}/owner/inventory?action=add"
               class="btn btn-success rounded-3 px-4">
                <i class="bi bi-plus-circle"></i>
                Thêm dụng cụ
            </a>
        </div>

        <div class="card border-0 rounded-4 mb-4" style="box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);">
            <div class="card-body">
                <form id="inventoryToolbarForm"
                      method="get"
                      action="${pageContext.request.contextPath}/owner/inventory"
                      class="row g-3 align-items-center inventory-search-form">

                    <input type="hidden" name="page" id="inventoryPageField" value="${currentPage}">

                    <div class="col-lg-4 col-md-6 inventory-search-input">
                        <div class="search-suggestion-wrap">
                            <input type="text"
                                   id="inventoryKeywordInput"
                                   name="keyword"
                                   value="${keyword}"
                                   class="form-control rounded-3"
                                   placeholder="Tìm theo tên dụng cụ"
                                   aria-label="Tìm theo tên dụng cụ"
                                   autocomplete="off">
                            <div id="inventorySuggestionMenu" class="search-suggestion-menu"></div>
                        </div>
                    </div>

                    <div class="col-auto">
                        <button type="submit" id="inventorySearchBtn" class="btn btn-outline-success rounded-3">
                            <i class="bi bi-search"></i>
                            Tìm kiếm
                        </button>
                    </div>

                    <div class="col-lg-2 col-md-3 col-sm-6">
                        <select name="priceSort"
                                class="form-select rounded-3"
                                aria-label="Lọc theo giá">
                            <option value="default" ${priceSort == 'default' ? 'selected' : ''}>Giá: Mặc định</option>
                            <option value="price_desc" ${priceSort == 'price_desc' ? 'selected' : ''}>Giá: Cao xuống thấp</option>
                            <option value="price_asc" ${priceSort == 'price_asc' ? 'selected' : ''}>Giá: Thấp lên cao</option>
                        </select>
                    </div>

                    <div class="col-lg-2 col-md-3 col-sm-6">
                        <select name="status"
                                class="form-select rounded-3"
                                aria-label="Lọc theo trạng thái">
                            <option value="all" ${status == 'all' ? 'selected' : ''}>Trạng thái: Tất cả</option>
                            <option value="active" ${status == 'active' ? 'selected' : ''}>Đang hoạt động</option>
                            <option value="inactive" ${status == 'inactive' ? 'selected' : ''}>Ngừng hoạt động</option>
                        </select>
                    </div>

                    <div class="col-auto">
                        <button type="submit" id="inventoryFilterBtn" class="btn btn-success rounded-3">
                            <i class="bi bi-funnel"></i>
                            Lọc
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <div class="card border-0 rounded-4" style="box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);">
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table align-middle">
                        <thead class="table-light">
                        <tr>
                            <th>STT</th>
                            <th>Tên dụng cụ</th>
                            <th>Thương hiệu</th>
                            <th>Giá thuê</th>
                            <th>Trạng thái</th>
                            <th class="text-end">Thao tác</th>
                        </tr>
                        </thead>

                        <tbody>
                        <c:forEach items="${inventories}" var="i" varStatus="loop">
                            <tr>
                                <td>${(currentPage - 1) * 10 + loop.count}</td>
                                <td class="fw-semibold">${i.name}</td>
                                <td>${i.brand}</td>
                                <td class="text-success fw-bold">${i.rentalPrice}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${i.active}">
                                            <span class="badge bg-success">Đang hoạt động</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">Ngừng hoạt động</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-end">
                                    <a href="${pageContext.request.contextPath}/owner/inventory?action=edit&id=${i.inventoryId}"
                                       class="btn btn-sm btn-outline-primary"
                                       aria-label="Chỉnh sửa">
                                        <i class="bi bi-pencil"></i>
                                    </a>

                                    <a href="${pageContext.request.contextPath}/owner/inventory?action=delete&id=${i.inventoryId}"
                                       onclick="return confirm('Bạn có chắc muốn xóa dụng cụ này không?')"
                                       class="btn btn-sm btn-outline-danger"
                                       aria-label="Xóa">
                                        <i class="bi bi-trash"></i>
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>

                        <c:if test="${empty inventories}">
                            <tr>
                                <td colspan="6" class="text-center text-muted">
                                    Không tìm thấy dụng cụ nào
                                </td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>

                <div id="inventorySuggestionDataset" class="d-none">
                    <c:forEach items="${suggestionInventories}" var="item">
                        <div class="suggestion-source-item">
                            <span class="suggestion-source-name"><c:out value="${item.name}"/></span>
                            <span class="suggestion-source-label"><c:out value="${item.name}"/></span>
                            <span class="suggestion-source-meta"><c:out value="${item.brand}"/></span>
                        </div>
                    </c:forEach>
                </div>

                <c:if test="${totalPages > 1}">
                    <c:if test="${currentPage > 1}">
                        <c:url var="previousPageUrl" value="/owner/inventory">
                            <c:param name="page" value="${currentPage - 1}"/>
                            <c:param name="keyword" value="${keyword}"/>
                            <c:param name="priceSort" value="${priceSort}"/>
                            <c:param name="status" value="${status}"/>
                        </c:url>
                    </c:if>

                    <c:if test="${currentPage < totalPages}">
                        <c:url var="lastPageUrl" value="/owner/inventory">
                            <c:param name="page" value="${totalPages}"/>
                            <c:param name="keyword" value="${keyword}"/>
                            <c:param name="priceSort" value="${priceSort}"/>
                            <c:param name="status" value="${status}"/>
                        </c:url>

                        <c:url var="nextPageUrl" value="/owner/inventory">
                            <c:param name="page" value="${currentPage + 1}"/>
                            <c:param name="keyword" value="${keyword}"/>
                            <c:param name="priceSort" value="${priceSort}"/>
                            <c:param name="status" value="${status}"/>
                        </c:url>
                    </c:if>

                    <nav class="mt-4">
                        <ul class="pagination justify-content-center align-items-center gap-2 compact-pagination">
                            <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                <c:choose>
                                    <c:when test="${currentPage == 1}">
                                        <span class="page-link-static" aria-label="Trang trước">
                                            <i class="bi bi-chevron-left"></i>
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <a class="page-link" aria-label="Trang trước" href="${previousPageUrl}">
                                            <i class="bi bi-chevron-left"></i>
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </li>

                            <li class="page-item active">
                                <span class="page-link-static">${currentPage}</span>
                            </li>

                            <c:if test="${currentPage + 1 < totalPages}">
                                <li class="page-item disabled">
                                    <span class="page-link-static pagination-ellipsis">...</span>
                                </li>
                            </c:if>

                            <c:if test="${currentPage < totalPages}">
                                <li class="page-item">
                                    <a class="page-link" href="${lastPageUrl}">${totalPages}</a>
                                </li>
                            </c:if>

                            <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                <c:choose>
                                    <c:when test="${currentPage == totalPages}">
                                        <span class="page-link-static" aria-label="Trang sau">
                                            <i class="bi bi-chevron-right"></i>
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <a class="page-link" aria-label="Trang sau" href="${nextPageUrl}">
                                            <i class="bi bi-chevron-right"></i>
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </li>
                        </ul>
                    </nav>
                </c:if>
            </div>
        </div>
    </div>

    <%@ include file="../layout/footer.jsp"%>

</div>

<script>
document.addEventListener("DOMContentLoaded", function () {
    const toolbarForm = document.getElementById("inventoryToolbarForm");
    const pageField = document.getElementById("inventoryPageField");
    const keywordInput = document.getElementById("inventoryKeywordInput");
    const suggestionMenu = document.getElementById("inventorySuggestionMenu");

    function normalizeText(value) {
        return (value || "")
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .toLowerCase()
            .trim();
    }

    function collectSuggestionItems(datasetId) {
        const uniqueItems = new Map();
        const nodes = document.querySelectorAll("#" + datasetId + " .suggestion-source-item");

        nodes.forEach(function (node) {
            const name = (node.querySelector(".suggestion-source-name")?.textContent || "").trim();
            const label = (node.querySelector(".suggestion-source-label")?.textContent || name).trim();
            const meta = (node.querySelector(".suggestion-source-meta")?.textContent || "").trim();
            const key = normalizeText(name);

            if (!name || uniqueItems.has(key)) {
                return;
            }

            uniqueItems.set(key, {
                name: name,
                label: label,
                meta: meta
            });
        });

        return Array.from(uniqueItems.values());
    }

    function hideSuggestionMenu() {
        if (!suggestionMenu) {
            return;
        }

        suggestionMenu.classList.remove("is-visible");
        suggestionMenu.innerHTML = "";
    }

    function renderSuggestionMenu(items) {
        if (!suggestionMenu || !keywordInput) {
            return;
        }

        suggestionMenu.innerHTML = "";

        if (!items.length) {
            const emptyState = document.createElement("div");
            emptyState.className = "search-suggestion-empty";
            emptyState.textContent = "Không có dụng cụ phù hợp trong 50 dữ liệu đầu tiên.";
            suggestionMenu.appendChild(emptyState);
            suggestionMenu.classList.add("is-visible");
            return;
        }

        items.forEach(function (item) {
            const button = document.createElement("button");
            const title = document.createElement("span");

            button.type = "button";
            button.className = "search-suggestion-item";
            title.className = "search-suggestion-title";
            title.textContent = item.label;
            button.appendChild(title);

            if (item.meta) {
                const meta = document.createElement("span");
                meta.className = "search-suggestion-meta";
                meta.textContent = item.meta;
                button.appendChild(meta);
            }

            button.addEventListener("mousedown", function (event) {
                event.preventDefault();
                keywordInput.value = item.name;
                hideSuggestionMenu();
                keywordInput.focus();
            });

            suggestionMenu.appendChild(button);
        });

        suggestionMenu.classList.add("is-visible");
    }

    function setupSuggestionAutocomplete() {
        if (!keywordInput || !suggestionMenu) {
            return;
        }

        const dataset = collectSuggestionItems("inventorySuggestionDataset");

        function refreshSuggestions() {
            const keyword = normalizeText(keywordInput.value);

            if (!keyword) {
                hideSuggestionMenu();
                return;
            }

            const matchedItems = dataset.filter(function (item) {
                return normalizeText(item.name).includes(keyword);
            });

            renderSuggestionMenu(matchedItems);
        }

        keywordInput.addEventListener("input", refreshSuggestions);
        keywordInput.addEventListener("focus", refreshSuggestions);

        document.addEventListener("click", function (event) {
            if (event.target === keywordInput || suggestionMenu.contains(event.target)) {
                return;
            }

            hideSuggestionMenu();
        });
    }

    if (toolbarForm && pageField) {
        toolbarForm.addEventListener("submit", function () {
            pageField.value = "1";
        });
    }

    setupSuggestionAutocomplete();
});
</script>
