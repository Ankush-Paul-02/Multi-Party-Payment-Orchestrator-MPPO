package com.example.group_service.business.shared;

import java.math.BigDecimal;

public record CreatePaymentIntentRequest(
        Long groupId,
        Long memberId,
        BigDecimal amount,
        String customerEmail
) {
}
