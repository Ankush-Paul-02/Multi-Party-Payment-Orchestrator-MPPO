package com.example.user_service.auth;

import com.example.user_service.auth.dto.AuthRequest;
import com.example.user_service.auth.dto.AuthResponse;
import com.example.user_service.auth.dto.RefreshTokenRequest;
import com.example.user_service.auth.dto.RegisterRequest;
import com.example.user_service.entity.RefreshToken;
import com.example.user_service.entity.User;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request, String ip, String userAgent) {
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Username already in use");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .build();

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user, ip, userAgent);

        return AuthResponse.bearer(accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse login(AuthRequest request, String ip, String userAgent) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(), request.password()
                )
        );

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user, ip, userAgent);

        return AuthResponse.bearer(accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request, String ip, String userAgent) {
        RefreshToken token = refreshTokenService.verifyAndGet(request.refreshToken());
        User user = token.getUser();

        // rotate refresh: revoke old, issue new
        refreshTokenService.revokeToken(token, ip);
        String newRefreshToken = refreshTokenService.createRefreshToken(user, ip, userAgent);
        String newAccessToken = jwtService.generateAccessToken(user);

        return AuthResponse.bearer(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(RefreshTokenRequest request, String ip) {
        RefreshToken token = refreshTokenService.verifyAndGet(request.refreshToken());
        refreshTokenService.revokeToken(token, ip);
        // Optionally: revokeAllForUser(token.getUser().getId(), ip) for global logout
    }
}
