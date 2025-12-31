package com.example.user_service.security;

import com.example.user_service.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateAccessToken(User user) {
        return generateToken(
                Map.of(
                        "role", user.getRole().name(),
                        "username", user.getUsername()
                ),
                user.getId(),
                accessTokenExpirationMs
        );
    }

    private String generateToken(Map<String, Object> extraClaims, String subject, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> extractor) {
        final Claims claims = parseClaims(token);
        return extractor.apply(claims);
    }

    public boolean isTokenValid(String token, User user) {
        final String userId = extractUserId(token);
        return userId.equals(user.getId())
                && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
