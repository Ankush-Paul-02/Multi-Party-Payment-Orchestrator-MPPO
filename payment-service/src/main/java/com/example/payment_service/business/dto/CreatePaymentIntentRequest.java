package com.example.payment_service.business.dto;

import java.math.BigDecimal;

public record CreatePaymentIntentRequest(
        Long groupId,
        Long memberId,
        BigDecimal amount,
        String customerEmail
) {}

