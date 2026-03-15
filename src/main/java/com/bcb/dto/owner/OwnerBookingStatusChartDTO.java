package com.bcb.dto.owner;

import java.math.BigDecimal;

public class OwnerBookingStatusChartDTO {
	
	private String label;       	// "Completed", "Pending"...
	private BigDecimal pct;     // 48.2
    private Integer count;          // 120 lượt , hiện khi hover
    private String color;       	// màu hex

    // Color map by status
    public static String resolveColor(String status) {
    	
        return switch (status.toUpperCase()) {
            case "COMPLETED"  -> "#064E3B";
            case "PENDING"    -> "#F59E0B";
            case "CONFIRMED"  -> "#A3E635";
            case "CANCELLED"  -> "#EF4444";
            case "EXPIRED"    -> "#9CA3AF";
            default           -> "#6B7280";
        };
    }

    public OwnerBookingStatusChartDTO () {}

    public OwnerBookingStatusChartDTO (String status, Integer count, BigDecimal pct) {
    	
        // Label capitalize: "COMPLETED" → "Completed"
        this.label = status.charAt(0) + status.substring(1).toLowerCase();
        this.count = count;
        this.pct = pct;
        this.color = resolveColor(status);
    }

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public BigDecimal getPct() {
		return pct;
	}

	public void setPct(BigDecimal pct) {
		this.pct = pct;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
}
