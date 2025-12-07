package com.example.payment_service.client;

import com.example.payment_service.config.RazorpayFeignConfig;
import com.example.payment_service.business.dto.RazorpayPaymentLinkRequest;
import com.example.payment_service.business.dto.RazorpayPaymentLinkResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "razorpayClient",
        url = "${razorpay.base-url}",
        configuration = RazorpayFeignConfig.class
)
public interface RazorpayClient {

    @PostMapping("/payment_links")
    RazorpayPaymentLinkResponse createPaymentLink(@RequestBody RazorpayPaymentLinkRequest request);
}
