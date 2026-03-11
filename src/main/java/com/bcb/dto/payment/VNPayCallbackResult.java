package com.bcb.dto.payment;

/**
 * Result of VNPay callback signature verification.
 * Pure transport object — no business logic attached.
 *
 * @author AnhTN
 */
public class VNPayCallbackResult {

    private boolean valid;
    private String txnRef;           // our transaction_code
    private String responseCode;     // VNPay vnp_ResponseCode
    private String vnpayTxnNo;       // VNPay vnp_TransactionNo
    private long amount;             // VND (already divided by 100)

    public VNPayCallbackResult() {}

    /** @return true if the HMAC signature matched */
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    /** @return our internal transaction reference (vnp_TxnRef) */
    public String getTxnRef() { return txnRef; }
    public void setTxnRef(String txnRef) { this.txnRef = txnRef; }

    /** @return VNPay response code; "00" means success */
    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    /** @return VNPay's own transaction number */
    public String getVnpayTxnNo() { return vnpayTxnNo; }
    public void setVnpayTxnNo(String vnpayTxnNo) { this.vnpayTxnNo = vnpayTxnNo; }

    /** @return payment amount in VND */
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
}

