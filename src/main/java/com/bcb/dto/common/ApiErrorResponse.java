package com.bcb.dto.common;

public class ApiErrorResponse {
    private final boolean success = false;
    private final String message;
    private final Object data;

    public ApiErrorResponse(String message) {
        this(message, null);
    }

    public ApiErrorResponse(String message, Object data) {
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
