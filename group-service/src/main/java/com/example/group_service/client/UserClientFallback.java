package com.example.group_service.client;

import org.springframework.stereotype.Component;

@Component
public class UserClientFallback  implements  UserClient {
    @Override
    public String getUserIdByEmail(String email) {
        throw new RuntimeException(
                "UserService is currently unavailable. Please try again later."
        );
    }
}
