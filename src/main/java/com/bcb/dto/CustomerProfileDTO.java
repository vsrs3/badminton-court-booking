package com.bcb.dto;

import jakarta.servlet.http.Part;

public class CustomerProfileDTO {
    private String email;
    private String fullName;
    private String phone;

    private String avatarPath;
    private Part avatarFile;

    public CustomerProfileDTO() {
    }

    public CustomerProfileDTO(String fullName, String email, String phone, Part avatarFile) {
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.avatarFile = avatarFile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public Part getAvatarFile() {
        return avatarFile;
    }

    public void setAvatarFile(Part avatarFile) {
        this.avatarFile = avatarFile;
    }
}
