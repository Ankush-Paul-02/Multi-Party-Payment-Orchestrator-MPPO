package com.example.user_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @GetMapping("/hi")
    public String sayHi(Principal principal) {
        return "Hi " + principal.getName() + ", User Service is running...";
    }
}
