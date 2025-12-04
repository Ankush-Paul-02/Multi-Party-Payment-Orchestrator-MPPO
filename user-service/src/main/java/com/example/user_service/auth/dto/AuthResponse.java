package com.example.user_service.auth.dto;


public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {
    public static AuthResponse bearer(String access, String refresh) {
        return new AuthResponse(access, refresh, "Bearer");
    }
}