package com.example.user_service.auth;

import com.example.user_service.entity.RefreshToken;
import com.example.user_service.entity.User;
import com.example.user_service.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private final SecureRandom secureRandom = new SecureRandom();

    public String createRefreshToken(User user, String ip, String userAgent) {
        String token = generateSecureRandomToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .revoked(false)
                .createdByIp(ip)
                .userAgent(userAgent)
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    private String generateSecureRandomToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Transactional
    public RefreshToken verifyAndGet(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeToken(RefreshToken token, String ip) {
        token.setRevoked(true);
        token.setRevokedByIp(ip);
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void revokeAllForUser(String userId, String ip) {
        refreshTokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(userId) && !t.isRevoked())
                .forEach(t -> {
                    t.setRevoked(true);
                    t.setRevokedByIp(ip);
                });
    }
}
