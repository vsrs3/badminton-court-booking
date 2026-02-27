package com.bcb.utils.singlebooking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Gson-based JSON read/write utility for single-booking module.
 *
 * @author AnhTN
 */
public final class SingleBookingJsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create();

    private SingleBookingJsonUtil() {}

    /**
     * Serializes an object to JSON string.
     */
    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    /**
     * Deserializes JSON string to target class.
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    /**
     * Reads JSON body from HttpServletRequest and deserializes to target class.
     */
    public static <T> T readBody(HttpServletRequest request, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return fromJson(sb.toString(), clazz);
    }
}
