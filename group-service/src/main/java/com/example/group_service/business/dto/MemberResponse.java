package com.example.group_service.business.dto;

import com.example.group_service.data.enums.MemberStatus;

import java.math.BigDecimal;

public record MemberResponse(
        Long memberId,
        String userId,
        String email,
        BigDecimal amountToPay,
        MemberStatus status
) {
}