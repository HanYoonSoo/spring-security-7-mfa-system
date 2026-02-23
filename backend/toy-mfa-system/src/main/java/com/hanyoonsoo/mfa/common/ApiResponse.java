package com.hanyoonsoo.mfa.common;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ApiResponse<T>(
        String code,
        HttpStatus httpStatus,
        String message,
        Instant timeStamp,
        T data
) {
    public ApiResponse(HttpStatus httpStatus) {
        this("0000", httpStatus, "성공", Instant.now(), null);
    }

    public ApiResponse(HttpStatus httpStatus, T data) {
        this("0000", httpStatus, "성공", Instant.now(), data);
    }

    public ApiResponse(String code, HttpStatus httpStatus, String message) {
        this(code, httpStatus, message, Instant.now(), null);
    }

    public static <T> ApiResponse<T> success(HttpStatus httpStatus, T data) {
        return new ApiResponse<>("0000", httpStatus, "성공", Instant.now(), data);
    }

    public static ApiResponse<Void> success(HttpStatus httpStatus) {
        return new ApiResponse<>("0000", httpStatus, "성공", Instant.now(), null);
    }

    public static <T> ApiResponse<T> fail(String code, HttpStatus httpStatus, String message) {
        return new ApiResponse<>(code, httpStatus, message, Instant.now(), null);
    }

    public static <T> ApiResponse<T> fail(String code, HttpStatus httpStatus, String message, T data) {
        return new ApiResponse<>(code, httpStatus, message, Instant.now(), data);
    }
}
