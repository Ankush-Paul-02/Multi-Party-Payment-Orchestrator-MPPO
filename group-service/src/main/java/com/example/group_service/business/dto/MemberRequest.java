package com.example.group_service.business.dto;

import java.math.BigDecimal;

public record MemberRequest(
        String userId, // not required in create group request, it'll attach automatically after validation of this dto
        String email,
        String phone, // (Optional - Twilio is not added in this project)
        BigDecimal customAmount
) {}
