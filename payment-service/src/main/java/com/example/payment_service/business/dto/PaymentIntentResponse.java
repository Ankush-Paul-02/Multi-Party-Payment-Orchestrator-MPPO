package com.example.payment_service.business.dto;

public record PaymentIntentResponse(
        String paymentId,
        String providerPaymentId,
        String hostedPaymentUrl
) {}

