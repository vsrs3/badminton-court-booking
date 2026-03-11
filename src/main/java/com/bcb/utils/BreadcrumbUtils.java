package com.bcb.utils;

import com.bcb.model.BreadcrumbItem;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for building breadcrumb navigation trails.
 * Sets both "breadcrumbItems" and "pageTitle" request attributes.
 */
public class BreadcrumbUtils {

    private final HttpServletRequest request;
    private final List<BreadcrumbItem> items = new ArrayList<>();
    private final String contextPath;

    private BreadcrumbUtils(HttpServletRequest request) {
        this.request = request;
        this.contextPath = request.getContextPath();
    }

    public static BreadcrumbUtils builder(HttpServletRequest request) {
        return new BreadcrumbUtils(request);
    }

    /** Add the "Dashboard" root item (linked) */
    public BreadcrumbUtils dashboard() {
        items.add(BreadcrumbItem.of("Dashboard", contextPath + "/owner/dashboard"));
        return this;
    }

    /** Add the "Địa Điểm" facility list item (linked) */
    public BreadcrumbUtils facilityList() {
        items.add(BreadcrumbItem.of("Địa Điểm", contextPath + "/owner/facility/list"));
        return this;
    }

    /** Add a specific facility name item (linked to its detail page) */
    public BreadcrumbUtils facility(String facilityName, int facilityId) {
        items.add(BreadcrumbItem.of(facilityName, contextPath + "/owner/facility/view/" + facilityId));
        return this;
    }

    /** Add the "Voucher Dashboard" item (linked) */
    public BreadcrumbUtils voucherDashboard() {
        items.add(BreadcrumbItem.of("Quản Lý Voucher", contextPath + "/owner/vouchers/dashboard"));
        return this;
    }

    /** Add the "Danh Sách Voucher" item (linked) */
    public BreadcrumbUtils voucherList() {
        items.add(BreadcrumbItem.of("Danh Sách Voucher", contextPath + "/owner/vouchers/list"));
        return this;
    }

    /** Add a generic linked item */
    public BreadcrumbUtils add(String label, String url) {
        items.add(BreadcrumbItem.of(label, url));
        return this;
    }

    /** Add the final active item (current page — no link) and set as page title */
    public BreadcrumbUtils active(String label) {
        items.add(BreadcrumbItem.active(label));
        return this;
    }

    /** Build and set request attributes: breadcrumbItems + pageTitle */
    public void build() {
        request.setAttribute("breadcrumbItems", items);

        // Use the last item's label as page title
        if (!items.isEmpty()) {
            request.setAttribute("pageTitle", items.get(items.size() - 1).getLabel());
        }
    }
}

