package com.bcb.dto.owner;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OwnerRevenueCardDTO {

    private BigDecimal currentAmount;   
    private BigDecimal previousAmount;  
    private BigDecimal changePercent;   
    private boolean isUp;           

    public OwnerRevenueCardDTO(BigDecimal current, BigDecimal previous) {

        this.currentAmount = current != null ? current : BigDecimal.ZERO;
        this.previousAmount = previous != null ? previous : BigDecimal.ZERO;

        //Constructor
        if (this.previousAmount.compareTo(BigDecimal.ZERO) == 0) {

            if (this.currentAmount.compareTo(BigDecimal.ZERO) > 0) {
                this.changePercent = new BigDecimal("100");
            } else {
                this.changePercent = BigDecimal.ZERO;
            }

            this.isUp = this.currentAmount.compareTo(BigDecimal.ZERO) >= 0;

        } else {

            this.changePercent = this.currentAmount
                    .subtract(this.previousAmount)
                    .divide(this.previousAmount, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(1, RoundingMode.HALF_UP);;

            this.isUp = this.changePercent.compareTo(BigDecimal.ZERO) >= 0;
        }
    }

    //Constructor
    public OwnerRevenueCardDTO() {
		super();
	}
    
    
	// Format hiển thị: "440,800 VND"
    public String getFormattedAmount() {
        return String.format("%,.0f VND", currentAmount);
    }

    // Format badge: "+12.5%" hoặc "-3.2%"
    public String getFormattedPercent() {
    	
    	 String sign = isUp ? "+" : "";
    	 return sign + changePercent.toPlainString() + "%";
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public BigDecimal getPreviousAmount() {
        return previousAmount;
    }

    public void setPreviousAmount(BigDecimal previousAmount) {
        this.previousAmount = previousAmount;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(BigDecimal changePercent) {
        this.changePercent = changePercent;
    }

    public boolean isUp() {
        return isUp;
    }

    public void setUp(boolean isUp) {
        this.isUp = isUp;
    }
}