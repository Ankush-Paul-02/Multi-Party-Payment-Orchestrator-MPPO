package com.example.payment_service.config;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
public class RazorpayFeignConfig {

    private final RazorpayProperties razorpayProperties;

    @Bean
    public RequestInterceptor authInterceptor() {
        return requestTemplate -> {
            String auth = razorpayProperties.getKeyId() + ":" + razorpayProperties.getKeySecret();
            String encoded = Base64.getEncoder()
                    .encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            requestTemplate.header("Authorization", "Basic " + encoded);
        };
    }
}
