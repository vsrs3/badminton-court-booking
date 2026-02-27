package com.bcb.model;

/**
 * Represents a single breadcrumb navigation item.
 * If url is null, the item is considered the current/active page.
 */
public class BreadcrumbItem {

    private String label;
    private String url;

    public BreadcrumbItem() {}

    public BreadcrumbItem(String label, String url) {
        this.label = label;
        this.url = url;
    }

    /** Create a linked breadcrumb item */
    public static BreadcrumbItem of(String label, String url) {
        return new BreadcrumbItem(label, url);
    }

    /** Create the active (current page) breadcrumb item — no link */
    public static BreadcrumbItem active(String label) {
        return new BreadcrumbItem(label, null);
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public boolean isActive() { return url == null; }
}
