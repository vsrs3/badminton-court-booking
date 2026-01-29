package com.bcb.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection context.
 * Loads DB configuration from db.properties and provides Connection instances.
 */
public class DBContext {

    private static final Properties props = new Properties();
    private static final String CONFIG_FILE = "/db.properties";

    static {
        // Khối static này sẽ chạy ngay khi class được load vào bộ nhớ
        try (InputStream input = DBContext.class.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Không tìm thấy file cấu hình: " + CONFIG_FILE);
            }
            // Nạp dữ liệu từ file properties vào đối tượng props
            props.load(input);

            // Kiểm tra và load Driver
            String driver = props.getProperty("db.driver");
            if (driver == null || driver.trim().isEmpty()) {
                throw new RuntimeException("Thuộc tính db.driver không có trong " + CONFIG_FILE);
            }
            Class.forName(driver);
            System.out.println(">>> Đã nạp Driver thành công!");

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đọc file cấu hình: " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Không tìm thấy thư viện JDBC Driver: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy một kết nối mới tới database.
     * Vì các biến là static, bạn có thể gọi DBContext.getConnection() ở bất cứ đâu.
     */
    public static Connection getConnection() {
        try {
            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            // Kiểm tra các thông số quan trọng
            if (url == null || url.trim().isEmpty()) {
                throw new RuntimeException("Thiếu db.url trong file cấu hình");
            }

            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kết nối đến SQL Server: " + e.getMessage(), e);
        }
    }

    // Hàm main để bạn test nhanh kết nối
    public static void main(String[] args) {
        try (Connection con = DBContext.getConnection()) {
            if (con != null) {
                System.out.println(">>> Chúc mừng! Kết nối đến SQL Server thành công.");
                System.out.println(">>> Database: " + con.getCatalog());
            }
        } catch (SQLException e) {
            System.err.println(">>> Kết nối thất bại: " + e.getMessage());
        }
    }
}