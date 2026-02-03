package com.bcb.service.impl;

import com.bcb.config.ConfigUpload;
import com.bcb.exception.BusinessException;
import com.bcb.service.UploadService;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class UploadServiceImpl implements UploadService {

    @Override
    public String saveImage(Part part, String subFolder)
            throws IOException, BusinessException {

        if (part == null || part.getSize() == 0) return null;

        String submitted = Paths.get(part.getSubmittedFileName())
                .getFileName().toString();

        if (submitted.isBlank()) return null;

        if (!part.getContentType().startsWith("image/")) {
            throw new BusinessException("Only image files allowed");
        }

        String ext = "";
        int dot = submitted.lastIndexOf('.');
        if (dot > 0) ext = submitted.substring(dot);

        String fileName = UUID.randomUUID() + ext;

        String rootPath = ConfigUpload.getUploadLocation();
        File uploadDir = new File(rootPath, subFolder);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        File file = new File(uploadDir, fileName);

        try (InputStream in = part.getInputStream()) {
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return subFolder + "/" + fileName;
    }

    @Override
    public void deleteFile(String relativePath) {

        if (relativePath == null || relativePath.isBlank()) return;

        String rootPath = ConfigUpload.getUploadLocation();
        File file = new File(rootPath, relativePath);

        if (file.exists() && file.isFile()) {
            if (!file.delete()) {
                System.err.println("Cannot delete file: " + file.getAbsolutePath());
            }
        }
    }
}