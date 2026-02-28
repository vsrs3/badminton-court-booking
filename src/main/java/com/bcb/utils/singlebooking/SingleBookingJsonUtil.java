package com.bcb.utils.singlebooking;

import com.google.gson.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Gson-based JSON read/write utility for single-booking module.
 * Registers custom serializers for java.time types.
 *
 * @author AnhTN
 */
public final class SingleBookingJsonUtil {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .registerTypeAdapter(LocalTime.class, (JsonSerializer<LocalTime>)
                    (src, typeOfSrc, ctx) -> new JsonPrimitive(src.format(TF)))
            .registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>)
                    (json, typeOfT, ctx) -> LocalTime.parse(json.getAsString(), TF))
            .create();

    private SingleBookingJsonUtil() {}

    /** Serializes an object to JSON string. */
    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    /** Deserializes JSON string to target class. */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    /** Reads JSON body from HttpServletRequest and deserializes to target class. */
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
