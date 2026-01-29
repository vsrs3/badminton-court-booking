package com.bcb.utils;


import com.bcb.dto.CustomerProfileDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DBUpload {

    public static String getAvatarPath(HttpServletRequest request, CustomerProfileDTO dto) throws IOException {
        String avatarPath = dto.getAvatarPath();

        if(dto.getAvatarFile() != null && dto.getAvatarFile().getSize() > 0) {

            String originalName = Paths.get(dto.getAvatarFile().getSubmittedFileName()).getFileName().toString();
            String fileName = System.currentTimeMillis() + "_" + originalName;

            //Upload into webapp folder
            String sourceUploadPath = getSourceUploadPath(request, "assets/images/account");
            File sourceFile = new File(sourceUploadPath + File.separator + fileName);
            dto.getAvatarFile().write(sourceFile.getAbsolutePath());

            //Upload into target folder
            String targetUploadPath = getTargetUploadPath(request, "assets/images/account");
            File targetFile = new File(targetUploadPath + File.separator + fileName);
            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            avatarPath = "assets/images/account/" + fileName;
        }
        return avatarPath;
    }

    /**
     * Lấy đường dẫn tới webapp (để lưu lâu dài) */
    public static String getSourceUploadPath(HttpServletRequest request, String relativePath) {
        // Lấy đường dẫn tại webapp
        String currentRunningPath  = request.getServletContext().getRealPath("/");
        // Kết quả: "D:\Kỳ 5\SWP391\other\test\target\bcb-1.0-SNAPSHOT\"

        File targetFolder = new File(currentRunningPath);
        File rootFolder = targetFolder.getParentFile().getParentFile(); //đến test
        File uploadFolder = new File(rootFolder, "src/main/webapp/" + relativePath);

        //Tạo thư mục nếu chưa có
        if (!uploadFolder.exists()) { uploadFolder.mkdirs(); }
        System.out.println("SOURCE upload path = " + uploadFolder.getAbsolutePath());

        return uploadFolder.getAbsolutePath();
    }

    /**
     * Lấy đường dẫn tới target (để server đọc ngay) */
    public static String getTargetUploadPath(HttpServletRequest request, String relativePath) {
        String currentRunningPath = request.getServletContext().getRealPath("/");
        File uploadFolder = new File(currentRunningPath, relativePath);

        //Tạo thư mục nếu chưa có
        if (!uploadFolder.exists()) { uploadFolder.mkdirs(); }
        System.out.println("TARGET upload path = " + uploadFolder.getAbsolutePath());

        return uploadFolder.getAbsolutePath();
    }
}

