package com.bcb.dto.voucher;

/**
 * DTO for Voucher filter/search request from AJAX.
 * Carries all filter, pagination and sort params.
 *
 * @author AnhTN
 */
public class VoucherFilterDTO {

    private String keyword;       // search by code or name
    private String status;        // DISABLED | UPCOMING | ACTIVE | EXPIRED
    private String discountType;  // PERCENTAGE | FIXED_AMOUNT
    private Integer facilityId;
    private String dateFrom;      // ISO date string
    private String dateTo;
    private String sortBy;        // code | name | discount_value | valid_from | valid_to
    private String sortDir;       // ASC | DESC
    private int page = 1;
    private int pageSize = 10;

    public VoucherFilterDTO() {}

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public Integer getFacilityId() { return facilityId; }
    public void setFacilityId(Integer facilityId) { this.facilityId = facilityId; }

    public String getDateFrom() { return dateFrom; }
    public void setDateFrom(String dateFrom) { this.dateFrom = dateFrom; }

    public String getDateTo() { return dateTo; }
    public void setDateTo(String dateTo) { this.dateTo = dateTo; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortDir() { return sortDir; }
    public void setSortDir(String sortDir) { this.sortDir = sortDir; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = Math.max(1, page); }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = Math.max(1, Math.min(100, pageSize)); }

    public int getOffset() { return (page - 1) * pageSize; }
}
