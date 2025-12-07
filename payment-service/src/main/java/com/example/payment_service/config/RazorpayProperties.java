package com.example.payment_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "razorpay")
public class RazorpayProperties {

    private String baseUrl;
    private String keyId;
    private String keySecret;
    private String webhookSecret;
}
