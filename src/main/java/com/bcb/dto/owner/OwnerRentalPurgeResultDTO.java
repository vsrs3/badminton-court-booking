package com.bcb.dto.owner;

public class OwnerRentalPurgeResultDTO {

    private int rentalLogDeleted;
    private int racketRentalDeleted;
    private int scheduleDeleted;

    public int getRentalLogDeleted() {
        return rentalLogDeleted;
    }

    public void setRentalLogDeleted(int rentalLogDeleted) {
        this.rentalLogDeleted = rentalLogDeleted;
    }

    public int getRacketRentalDeleted() {
        return racketRentalDeleted;
    }

    public void setRacketRentalDeleted(int racketRentalDeleted) {
        this.racketRentalDeleted = racketRentalDeleted;
    }

    public int getScheduleDeleted() {
        return scheduleDeleted;
    }

    public void setScheduleDeleted(int scheduleDeleted) {
        this.scheduleDeleted = scheduleDeleted;
    }
}
