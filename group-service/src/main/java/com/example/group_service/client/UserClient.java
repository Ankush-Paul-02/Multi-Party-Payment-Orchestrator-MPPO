package com.example.group_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "user-service",
        path = "/api/v1/user",
        fallback = UserClientFallback.class
)
public interface UserClient {

    @GetMapping("/id-by-email")
    String getUserIdByEmail(@RequestParam String email);
}
