package com.example.user_service.controller;

import com.example.user_service.auth.AuthService;
import com.example.user_service.auth.dto.AuthRequest;
import com.example.user_service.auth.dto.AuthResponse;
import com.example.user_service.auth.dto.RefreshTokenRequest;
import com.example.user_service.auth.dto.RegisterRequest;
import com.example.user_service.business.dto.DefaultResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /* ---------------------------------------------------------
     * Register
     * --------------------------------------------------------- */
    @PostMapping("/register")
    public ResponseEntity<DefaultResponseDto<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest
    ) {
        var client = getClientInfo(servletRequest);
        var result = authService.register(request, client.ip(), client.userAgent());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new DefaultResponseDto<>(
                        DefaultResponseDto.Status.SUCCESS,
                        result,
                        "User registered successfully"
                ));
    }

    /* ---------------------------------------------------------
     * Login
     * --------------------------------------------------------- */
    @PostMapping("/login")
    public ResponseEntity<DefaultResponseDto<AuthResponse>> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletRequest servletRequest
    ) {
        var client = getClientInfo(servletRequest);
        var result = authService.login(request, client.ip(), client.userAgent());

        return ResponseEntity.ok(
                new DefaultResponseDto<>(
                        DefaultResponseDto.Status.SUCCESS,
                        result,
                        "Login successful"
                )
        );
    }

    /* ---------------------------------------------------------
     * Refresh Token
     * --------------------------------------------------------- */
    @PostMapping("/refresh")
    public ResponseEntity<DefaultResponseDto<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest servletRequest
    ) {
        var client = getClientInfo(servletRequest);
        var result = authService.refresh(request, client.ip(), client.userAgent());

        return ResponseEntity.ok(
                new DefaultResponseDto<>(
                        DefaultResponseDto.Status.SUCCESS,
                        result,
                        "Token refreshed successfully"
                )
        );
    }

    /* ---------------------------------------------------------
     * Logout
     * --------------------------------------------------------- */
    @PostMapping("/logout")
    public ResponseEntity<DefaultResponseDto<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest servletRequest
    ) {
        var client = getClientInfo(servletRequest);
        authService.logout(request, client.ip());

        return ResponseEntity
                .ok(new DefaultResponseDto<>(
                        DefaultResponseDto.Status.SUCCESS,
                        null,
                        "Logged out successfully"
                ));
    }

    /* ---------------------------------------------------------
     * Helpers
     * --------------------------------------------------------- */
    private ClientInfo getClientInfo(HttpServletRequest request) {
        return new ClientInfo(
                extractClientIp(request),
                request.getHeader("User-Agent")
        );
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    private record ClientInfo(String ip, String userAgent) {
    }
}
