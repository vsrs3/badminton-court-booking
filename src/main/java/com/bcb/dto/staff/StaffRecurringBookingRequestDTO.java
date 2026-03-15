package com.bcb.dto.staff;

import java.util.List;

public class StaffRecurringBookingRequestDTO {
    private String startDate;
    private String endDate;
    private String customerType;
    private Integer accountId;
    private String guestName;
    private String guestPhone;
    private String guestEmail;
    private String conflictPolicy;
    private String paymentMethod;
    private List<PatternDTO> patterns;
    private List<SelectedSessionDTO> selectedSessions;

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }
    public Integer getAccountId() { return accountId; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }
    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    public String getConflictPolicy() { return conflictPolicy; }
    public void setConflictPolicy(String conflictPolicy) { this.conflictPolicy = conflictPolicy; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public List<PatternDTO> getPatterns() { return patterns; }
    public void setPatterns(List<PatternDTO> patterns) { this.patterns = patterns; }
    public List<SelectedSessionDTO> getSelectedSessions() { return selectedSessions; }
    public void setSelectedSessions(List<SelectedSessionDTO> selectedSessions) { this.selectedSessions = selectedSessions; }

    public static class PatternDTO {
        private Integer dayOfWeek;
        private Integer courtId;
        private List<Integer> slotIds;

        public Integer getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
        public Integer getCourtId() { return courtId; }
        public void setCourtId(Integer courtId) { this.courtId = courtId; }
        public List<Integer> getSlotIds() { return slotIds; }
        public void setSlotIds(List<Integer> slotIds) { this.slotIds = slotIds; }
    }

    public static class SelectedSessionDTO {
        private String date;
        private Integer courtId;
        private List<Integer> slotIds;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Integer getCourtId() { return courtId; }
        public void setCourtId(Integer courtId) { this.courtId = courtId; }
        public List<Integer> getSlotIds() { return slotIds; }
        public void setSlotIds(List<Integer> slotIds) { this.slotIds = slotIds; }
    }
}
