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
        try (InputStream input = DBContext.class.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Configuration file not found: " + CONFIG_FILE);
            }
            props.load(input);

            String driver = props.getProperty("db.driver");
            if (driver == null || driver.trim().isEmpty()) {
                throw new RuntimeException("db.driver property not found in " + CONFIG_FILE);
            }

            Class.forName(driver);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database configuration: " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load JDBC driver: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a new database connection.
     *
     * @return Connection to the database
     * @throws RuntimeException if connection fails
     */
    public static Connection getConnection() {
        try {
            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            if (url == null || url.trim().isEmpty()) {
                throw new RuntimeException("db.url property not found");
            }
            if (username == null || username.trim().isEmpty()) {
                throw new RuntimeException("db.username property not found");
            }
            if (password == null) {
                throw new RuntimeException("db.password property not found");
            }

            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e){
            throw new RuntimeException("Failed to establish database connection: " + e.getMessage(), e);
        }
    }
}
