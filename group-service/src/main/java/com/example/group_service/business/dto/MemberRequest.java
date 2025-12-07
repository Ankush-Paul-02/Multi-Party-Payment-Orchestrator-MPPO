package com.example.group_service.business.dto;

import java.math.BigDecimal;

public record MemberRequest(
        String userId,
        String email,
        String phone,
        BigDecimal customAmount
) {}
