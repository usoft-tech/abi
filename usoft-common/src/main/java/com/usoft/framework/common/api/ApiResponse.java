package com.usoft.framework.common.api;

import java.time.Instant;

/**
 * 提供统一的API响应体结构
 */
public class ApiResponse<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;
    private Instant timestamp;

    /**
     * 创建成功响应
     */
    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.code = "OK";
        r.message = "success";
        r.data = data;
        r.timestamp = Instant.now();
        return r;
    }

    /**
     * 创建失败响应
     */
    public static <T> ApiResponse<T> fail(String code, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.code = code;
        r.message = message;
        r.timestamp = Instant.now();
        return r;
    }

    /**
     * 创建失败响应
     */
    public static <T> ApiResponse<T> fail(int code, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.code = Integer.toString(code);
        r.message = message;
        r.timestamp = Instant.now();
        return r;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}

