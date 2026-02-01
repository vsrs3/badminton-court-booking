package com.bcb.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUpload {

    private static final Properties props = new Properties();
    private static boolean loaded = false;

    private static synchronized void load() {
        if (loaded) return;

        try (InputStream input = ConfigUpload.class.getClassLoader()
                .getResourceAsStream("upload.properties")) {

            if (input == null) {
                throw new RuntimeException("Không tìm thấy upload.properties trong classpath!");
            }

            props.load(input);
            loaded = true;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi load upload.properties", e);
        }
    }

    public static String getUploadLocation() {
        load();  // lazy load
        String loc = props.getProperty("upload.location");
        if (loc == null) {
            throw new RuntimeException("upload.location không được config trong upload.properties");
        }

        // Nếu config relative (ví dụ: "uploads"), nối với CATALINA_BASE
        if (!new File(loc).isAbsolute()) {
            String catalinaBase = System.getProperty("catalina.base");
            if (catalinaBase != null) {
                loc = new File(catalinaBase, loc).getAbsolutePath();
            }
        }

        return loc;
    }
}
