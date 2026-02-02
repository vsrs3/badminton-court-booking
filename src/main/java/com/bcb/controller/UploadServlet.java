package com.bcb.controller;

import com.bcb.config.ConfigUpload;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


@WebServlet("/uploads/*")
public class UploadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo(); // ví dụ: /facility/facility1_1.jpg

        String root = ConfigUpload.getUploadLocation();

        File file = new File(root, path);


        if (!file.exists() || !file.isFile() || !file.canRead()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found: " + path);
            return;
        }

        String mimeType = getServletContext().getMimeType(file.getName());
        if (mimeType == null) mimeType = "image/jpeg"; // fallback cho jpg
        resp.setContentType(mimeType);
        resp.setContentLengthLong(file.length());
        resp.setHeader("Cache-Control", "no-cache");

        try {
            Files.copy(file.toPath(), resp.getOutputStream());
            resp.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, "Error serving file");
        }
    }
}