package com.bcb.config;

import java.io.InputStream;
import java.util.Properties;

public class ConfigMail {
	private static final Properties props = new Properties();
	
	static {
        try (InputStream input = ConfigMail.class
                .getClassLoader()
                .getResourceAsStream("mail.properties")) {

        	props.load(input);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
	
}
