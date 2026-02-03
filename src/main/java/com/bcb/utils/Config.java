/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bcb.utils;
import java.io.InputStream;
import java.util.Properties;
/**
 *
 * @author Nguyen Minh Duc
 */


public class Config {
    private static Properties props = new Properties();

    static {
        try (InputStream is = Config.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (is == null) {
                throw new RuntimeException("❌ Không tìm thấy file config.properties");
            }

            props.load(is);

        } catch (Exception e) {
            throw new RuntimeException("❌ Lỗi load config.properties", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}
   

