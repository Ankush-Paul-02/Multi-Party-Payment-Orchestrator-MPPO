package com.example.group_service.business.shared;

public record PaymentIntentResponse(
        String paymentId,
        String providerPaymentId,
        String hostedPaymentUrl
) {
}
