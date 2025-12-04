package com.example.api_gateway.config;

import com.example.api_gateway.security.JwtAuthenticationManager;
import com.example.api_gateway.security.JwtServerAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationManager jwtAuthManager;
    private final JwtServerAuthenticationConverter jwtConverter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(jwtAuthManager);
        jwtFilter.setServerAuthenticationConverter(jwtConverter);

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                .authorizeExchange(ex -> ex
                        .pathMatchers(
                                "/actuator/**",
                                "/eureka/**",
                                "/api/v1/auth/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )

                .build();
    }
}
