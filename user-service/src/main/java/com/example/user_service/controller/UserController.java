package com.example.user_service.controller;

import com.example.user_service.business.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/hi")
    public String sayHi(Principal principal) {
        return "Hi " + principal.getName() + ", User Service is running...";
    }

    @GetMapping("/id-by-email")
    public ResponseEntity<String> getUserIdByEmail(
            @RequestParam String email
    ) {
        String userId = userService.getUserIdByEmail(email);
        return ResponseEntity.ok(userId);
    }
}
