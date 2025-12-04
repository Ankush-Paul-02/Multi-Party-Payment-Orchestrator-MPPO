package com.example.api_gateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {

        String token = authentication.getCredentials().toString();

        try {
            Claims claims = jwtUtil.parseClaims(token);

            if (jwtUtil.isExpired(claims.getExpiration())) {
                return Mono.empty();
            }

            String username = claims.getSubject();
            String role = (String) claims.get("role");

            Authentication auth = new AbstractAuthenticationToken(
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            ) {
                @Override
                public Object getCredentials() {
                    return token;
                }

                @Override
                public Object getPrincipal() {
                    return username;
                }

                @Override
                public boolean isAuthenticated() {
                    return true;
                }
            };

            return Mono.just(auth);

        } catch (Exception ex) {
            log.warn("Invalid JWT: {}", ex.getMessage());
            return Mono.empty();
        }
    }
}
