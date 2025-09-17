package com.example.mqtt.controller;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(200, "成功", null);
    }
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "成功", data);
    }
    public static <T> ApiResponse<T> error(String msg) {
        return new ApiResponse<>(500, msg, null);
    }
}


