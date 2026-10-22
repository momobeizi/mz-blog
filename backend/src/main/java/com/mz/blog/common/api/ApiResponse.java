package com.mz.blog.common.api;

import java.util.UUID;

public record ApiResponse<T>(int code, String message, T data, String traceId) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "success", data, newTraceId());
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(0, "success", null, newTraceId());
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, newTraceId());
    }

    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data, newTraceId());
    }

    private static String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
