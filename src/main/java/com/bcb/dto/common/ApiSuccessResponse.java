package com.bcb.dto.common;

public class ApiSuccessResponse<T> {
    private final boolean success = true;
    private final String message;
    private final T data;

    public ApiSuccessResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
