<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<div class="table-responsive">
    <table class="table table-hover align-middle mb-0" id="pricingTable">
        <thead>
            <tr>
                <th class="px-4" style="width: 40%">Khung giờ</th>
                <th style="width: 40%">Giá / giờ (VND)</th>
                <th class="text-end px-4" style="width: 20%">Hành động</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${empty viewData.timeSlotPrices}">
                    <tr>
                        <td colspan="3" class="text-center text-muted py-5">
                            <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                            Chưa cấu hình khoảng giá nào
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${viewData.timeSlotPrices}" var="slot">
                        <tr data-price-id="${slot.priceId}">
                            <td class="px-4">
                                <span class="fw-semibold text-emerald">${slot.startTimeFormatted} – ${slot.endTimeFormatted}</span>
                            </td>
                            <td class="price-cell">
                                <span class="price-text fw-bold text-dark" data-value="${slot.price}">
                                    <fmt:formatNumber value="${slot.price}" pattern="#,###"/>
                                </span>
                            </td>
                            <td class="text-end px-4">
                                <button class="btn btn-sm btn-outline-warning me-1"
                                        onclick="openEditModal(${slot.priceId}, '${slot.startTimeFormatted}', '${slot.endTimeFormatted}', ${slot.price})"
                                        title="Chỉnh sửa">
                                    <i class="bi bi-pencil"></i>
                                </button>
                                <button class="btn btn-sm btn-outline-danger"
                                        onclick="confirmDelete(${slot.priceId})"
                                        title="Xóa">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
</div>

<c:if test="${not empty viewData.timeSlotPrices}">
    <div class="card-footer bg-light border-top">
        <button class="btn btn-success" onclick="openCreateModal()">
            <i class="bi bi-plus-circle me-1"></i> Thêm khoảng giá mới
        </button>
    </div>
</c:if>

<c:if test="${empty viewData.timeSlotPrices}">
    <div class="p-4 text-center">
        <button class="btn btn-success" onclick="openCreateModal()">
            <i class="bi bi-plus-circle me-1"></i> Tạo khoảng giá đầu tiên
        </button>
    </div>
</c:if>

