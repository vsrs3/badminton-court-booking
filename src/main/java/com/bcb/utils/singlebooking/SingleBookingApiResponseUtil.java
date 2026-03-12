package com.bcb.utils.singlebooking;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Utility for writing standard JSON success/error responses for single-booking API.
 *
 * @author AnhTN
 */
public final class SingleBookingApiResponseUtil {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private SingleBookingApiResponseUtil() {}

    /**
     * Writes a success JSON response.
     */
    public static void writeSuccess(HttpServletResponse resp, int httpStatus, Object data) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("data", data);
        write(resp, httpStatus, body);
    }

    /**
     * Writes an error JSON response.
     */
    public static void writeError(HttpServletResponse resp, int httpStatus, String code, String message,
                                  List<Map<String, Object>> details) throws IOException {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message);
        if (details != null && !details.isEmpty()) {
            error.put("details", details);
        }
        error.put("traceId", UUID.randomUUID().toString());
        error.put("timestamp", LocalDateTime.now().format(TS_FMT));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("error", error);
        write(resp, httpStatus, body);
    }

    private static void write(HttpServletResponse resp, int httpStatus, Map<String, Object> body) throws IOException {
        resp.setStatus(httpStatus);
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(SingleBookingJsonUtil.toJson(body));
        resp.getWriter().flush();
    }
}

