package com.example.payment_service.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayPaymentLinkRequest {

    private int amount;         // in paise
    private String currency;    // INR
    private String description;

    @JsonProperty("customer")
    private Customer customer;

    @JsonProperty("notify")
    private Notify notify;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty("callback_method")
    private String callbackMethod;

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
    public static class Customer {
        private String name;
        private String contact;
        private String email;
    }

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
    public static class Notify {
        private boolean sms;
        private boolean email;
    }
}
