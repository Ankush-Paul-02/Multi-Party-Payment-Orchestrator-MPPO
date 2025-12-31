package com.example.group_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignGatewayHeaderConfig {

    @Bean
    public feign.RequestInterceptor gatewayHeaderInterceptor() {
        return template -> {
            var attrs =
                    (org.springframework.web.context.request.ServletRequestAttributes)
                            org.springframework.web.context.request.RequestContextHolder
                                    .getRequestAttributes();

            if (attrs != null) {
                var request = attrs.getRequest();

                String userId = request.getHeader("X-USER-ID");
                String roles = request.getHeader("X-ROLES");

                if (userId != null) {
                    template.header("X-USER-ID", userId);
                }
                if (roles != null) {
                    template.header("X-ROLES", roles);
                }
            }
        };
    }
}
