package com.example.group_service.business.dto;

import com.example.group_service.data.enums.GroupStatus;
import com.example.group_service.data.enums.SplitType;

import java.math.BigDecimal;
import java.util.List;

public record GroupResponse(
        Long groupId,
        String hostUserId,
        GroupStatus status,
        SplitType splitType,
        BigDecimal totalAmount,
        BigDecimal requiredPaidAmount,
        List<MemberResponse> members
) {
}