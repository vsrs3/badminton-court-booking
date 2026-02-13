package com.bcb.service;

import com.bcb.exception.BusinessException;
import jakarta.servlet.http.Part;

import java.io.IOException;

public interface UploadService {

    String saveImage(Part part, String subFolder)
            throws IOException, BusinessException;

    void deleteFile(String relativePath);
}