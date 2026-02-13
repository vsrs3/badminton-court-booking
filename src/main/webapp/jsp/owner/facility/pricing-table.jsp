<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<div class="table-responsive">
    <table class="table table-hover align-middle mb-0" id="pricingTable">
        <thead>
            <tr>
                <th class="px-4" style="width: 40%">Khung giờ</th>
                <th style="width: 40%">Giá (VND)</th>
                <th class="text-end px-4" style="width: 20%">Hành động</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${viewData.timeSlotPrices}" var="slot">
                <tr data-slot-id="${slot.slotId}">
                    <td class="px-4">
                        <span class="fw-semibold text-emerald">${slot.startTimeFormatted} – ${slot.endTimeFormatted}</span>
                    </td>
                    <td class="price-cell">
                        <c:choose>
                            <c:when test="${not empty slot.price}">
                                <span class="price-text fw-bold text-dark" data-value="${slot.price}">
                                    <fmt:formatNumber value="${slot.price}" pattern="#,###"/>
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted price-text" data-value="">—</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td class="text-end px-4">
                        <button class="btn btn-sm btn-outline-warning" 
                                onclick="enterEditMode(this, ${slot.slotId})">
                            <i class="bi bi-pencil"></i>
                        </button>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>
