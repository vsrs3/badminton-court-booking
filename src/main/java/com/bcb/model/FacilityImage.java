package com.bcb.model;

import java.time.LocalDateTime;

public class FacilityImage {
    private int imageId;
    private int facilityId;
    private String imagePath;
    private boolean isThumbnail;
    private LocalDateTime createdAt;

    public FacilityImage() {}

    public FacilityImage(int imageId, int facilityId, String imagePath, boolean isThumbnail, LocalDateTime createdAt) {
        this.imageId = imageId;
        this.facilityId = facilityId;
        this.imagePath = imagePath;
        this.isThumbnail = isThumbnail;
        this.createdAt = createdAt;
    }

    public FacilityImage(int facilityId, String imagePath, boolean isThumbnail) {
        this.facilityId = facilityId;
        this.imagePath = imagePath;
        this.isThumbnail = isThumbnail;
    }

    public int getImageId() { return imageId; }
    public void setImageId(int imageId) { this.imageId = imageId; }
    public int getFacilityId() { return facilityId; }
    public void setFacilityId(int facilityId) { this.facilityId = facilityId; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public boolean isThumbnail() { return isThumbnail; }
    public void setThumbnail(boolean thumbnail) { isThumbnail = thumbnail; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "FacilityImage{" + "imageId=" + imageId + ", facilityId=" + facilityId +
               ", imagePath='" + imagePath + '\'' + ", isThumbnail=" + isThumbnail + '}';
    }
}
