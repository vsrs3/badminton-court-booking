package com.bcb.dto;

public class CustomerChangePassDTO {
    private String oldPass;
    private String newPass;
    private String confirmNewPass;

    public CustomerChangePassDTO(String oldPass, String newPass, String confirmNewPass) {
        this.oldPass = oldPass;
        this.newPass = newPass;
        this.confirmNewPass = confirmNewPass;
    }

    public String getOldPass() {
        return oldPass;
    }

    public void setOldPass(String oldPass) {
        this.oldPass = oldPass;
    }

    public String getNewPass() {
        return newPass;
    }

    public void setNewPass(String newPass) {
        this.newPass = newPass;
    }

    public String getConfirmNewPass() {
        return confirmNewPass;
    }

    public void setConfirmNewPass(String confirmNewPass) {
        this.confirmNewPass = confirmNewPass;
    }
}
