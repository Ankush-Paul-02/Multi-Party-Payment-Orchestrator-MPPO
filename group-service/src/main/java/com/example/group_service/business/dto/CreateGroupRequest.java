package com.example.group_service.business.dto;

import com.example.group_service.data.enums.SplitType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CreateGroupRequest(
        String cartId,
        BigDecimal totalAmount,
        Instant expiresAt,
        SplitType splitType,
        List<MemberRequest> members
) {}
