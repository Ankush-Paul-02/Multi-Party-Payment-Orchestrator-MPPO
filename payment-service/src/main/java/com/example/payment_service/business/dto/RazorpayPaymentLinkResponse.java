package com.example.payment_service.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayPaymentLinkResponse {

    private String id;

    @JsonProperty("short_url")
    private String shortUrl;

    private String status;
}

