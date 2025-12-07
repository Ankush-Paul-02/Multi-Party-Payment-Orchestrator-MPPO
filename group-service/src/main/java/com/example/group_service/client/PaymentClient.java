package com.example.group_service.client;

import com.example.group_service.business.shared.CreatePaymentIntentRequest;
import com.example.group_service.business.shared.PaymentIntentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAYMENT-SERVICE", path = "/api/v1/payment")
public interface PaymentClient {

    @PostMapping("/intents")
    PaymentIntentResponse createIntent(@RequestBody CreatePaymentIntentRequest request);
}
