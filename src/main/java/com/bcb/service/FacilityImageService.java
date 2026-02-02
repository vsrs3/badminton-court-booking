package com.bcb.service;

import com.bcb.exception.BusinessException;
import com.bcb.model.FacilityImage;
import java.util.List;

public interface FacilityImageService {
    FacilityImage getThumbnail(int facilityId) throws BusinessException;
    List<FacilityImage> getGallery(int facilityId) throws BusinessException;
    FacilityImage getImageById(int imageId) throws BusinessException;
    int addImage(FacilityImage newImage) throws BusinessException;
    void deleteImage(int imageId) throws BusinessException;
    void update(FacilityImage currentThumbnail);
}
