package com.bcb.model;

import java.time.LocalDateTime;

/**
 * Entity representing facility images
 */
public class FacilityImage {

    private Integer imageId;
    private Integer facilityId;
    private String imagePath;
    private Boolean isThumbnail;
    private LocalDateTime createdAt;

    // Constructors
    public FacilityImage() {
    }

    public FacilityImage(Integer imageId, Integer facilityId, String imagePath,
                         Boolean isThumbnail, LocalDateTime createdAt) {
        this.imageId = imageId;
        this.facilityId = facilityId;
        this.imagePath = imagePath;
        this.isThumbnail = isThumbnail;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Integer getImageId() {
        return imageId;
    }

    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }

    public Integer getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Integer facilityId) {
        this.facilityId = facilityId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Boolean getIsThumbnail() {
        return isThumbnail;
    }

    public void setIsThumbnail(Boolean isThumbnail) {
        this.isThumbnail = isThumbnail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}