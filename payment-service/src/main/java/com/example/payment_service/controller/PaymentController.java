package com.example.payment_service.controller;

import com.example.payment_service.business.dto.CreatePaymentIntentRequest;
import com.example.payment_service.business.dto.PaymentIntentResponse;
import com.example.payment_service.business.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/hi")
    public String sayHi(Principal principal) {
        return "Hi " + principal.getName() + ", Payment Service is running...";
    }

    @PostMapping("/intents")
    public ResponseEntity<PaymentIntentResponse> createIntent(
            @RequestBody @Valid CreatePaymentIntentRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createIntent(request));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        paymentService.processWebhook(payload, signature);
        return ResponseEntity.ok("ok");
    }
}
