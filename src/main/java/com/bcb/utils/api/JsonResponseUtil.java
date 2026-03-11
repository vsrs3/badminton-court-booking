package com.bcb.utils.api;

import com.bcb.dto.common.ApiErrorResponse;
import com.bcb.dto.common.ApiSuccessResponse;
import com.google.gson.Gson;

public final class JsonResponseUtil {

    private static final Gson GSON = new Gson();

    private JsonResponseUtil() {
    }

    public static String error(String message) {
        return GSON.toJson(new ApiErrorResponse(message));
    }

    public static String error(String message, Object data) {
        return GSON.toJson(new ApiErrorResponse(message, data));
    }

    public static <T> String success(String message, T data) {
        return GSON.toJson(new ApiSuccessResponse<>(message, data));
    }
}
