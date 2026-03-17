package com.bcb.dto.recurring;

import java.util.List;

/**
 * Request body for recurring confirm-and-pay.
 *
 * @author AnhTN
 */
public class RecurringConfirmRequestDTO {

    private String previewToken;
    private List<String> skipDates; // YYYY-MM-DD
    private String voucherCode;
    private List<RecurringModifiedSessionDTO> modifiedSessions;

    public String getPreviewToken() {
        return previewToken;
    }

    public void setPreviewToken(String previewToken) {
        this.previewToken = previewToken;
    }

    public List<String> getSkipDates() {
        return skipDates;
    }

    public void setSkipDates(List<String> skipDates) {
        this.skipDates = skipDates;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public List<RecurringModifiedSessionDTO> getModifiedSessions() {
        return modifiedSessions;
    }

    public void setModifiedSessions(List<RecurringModifiedSessionDTO> modifiedSessions) {
        this.modifiedSessions = modifiedSessions;
    }
}


