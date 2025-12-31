package com.example.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "GROUP-SERVICE", path = "/api/v1/group")
public interface GroupClient {

    @PostMapping("/{groupId}/members/{memberId}/paid")
    void markMemberPaid(@PathVariable Long groupId, @PathVariable Long memberId);
}

