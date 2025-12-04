package com.example.user_service.business.dto;

public record DefaultResponseDto<T>(
        Status status,
        T data,
        String message
) {
    public enum Status {
        FAILED,
        SUCCESS
    }
}
