package com.example.group_service.business.shared;

import org.apache.kafka.common.protocol.types.Field;

public record PaymentIntentResponse(
        String paymentId,
        Field.Str providerPaymentId,
        String hostedPaymentUrl
) {
}
