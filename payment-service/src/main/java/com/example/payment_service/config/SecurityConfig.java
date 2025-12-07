package com.example.payment_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new GatewayAuthFilter(), UsernamePasswordAuthenticationFilter.class)

                // Disable basic auth properly for Spring Security 6
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    /**
     * Custom filter that reads X-USER-ID from gateway.
     */
    static class GatewayAuthFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {

            String userId = request.getHeader("X-USER-ID");

            if (userId != null && !userId.isEmpty()) {

                String roles = request.getHeader("X-ROLES");

                AbstractAuthenticationToken auth = getAbstractAuthenticationToken(roles, userId);

                auth.setAuthenticated(true);

                org.springframework.security.core.context.SecurityContextHolder
                        .getContext()
                        .setAuthentication(auth);
            }

            filterChain.doFilter(request, response);
        }

        private static AbstractAuthenticationToken getAbstractAuthenticationToken(String roles, String userId) {
            List<SimpleGrantedAuthority> authorities =
                    roles != null && !roles.isEmpty()
                            ? List.of(new SimpleGrantedAuthority(roles))
                            : List.of();

            AbstractAuthenticationToken auth =
                    new AbstractAuthenticationToken(authorities) {
                        @Override
                        public Object getCredentials() {
                            return null;
                        }

                        @Override
                        public Object getPrincipal() {
                            return userId;
                        }
                    };
            return auth;
        }
    }
}
