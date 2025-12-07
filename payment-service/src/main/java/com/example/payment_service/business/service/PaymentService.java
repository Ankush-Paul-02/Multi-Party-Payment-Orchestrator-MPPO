package com.example.payment_service.business.service;

import com.example.payment_service.business.dto.CreatePaymentIntentRequest;
import com.example.payment_service.business.dto.PaymentIntentResponse;
import com.example.payment_service.business.dto.RazorpayPaymentLinkRequest;
import com.example.payment_service.business.dto.RazorpayPaymentLinkResponse;
import com.example.payment_service.client.RazorpayClient;
import com.example.payment_service.config.RazorpayProperties;
import com.example.payment_service.data.entities.Payment;
import com.example.payment_service.data.enums.PaymentStatus;
import com.example.payment_service.data.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final RazorpayProperties razorpayProperties;


    @Transactional
    public PaymentIntentResponse createIntent(CreatePaymentIntentRequest request) {
        Payment newPayment = paymentRepository.save(
                Payment.builder()
                        .groupId(request.groupId())
                        .memberId(request.memberId())
                        .amount(request.amount())
                        .provider("RAZORPAY")
                        .status(PaymentStatus.PENDING)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()
        );

        int paise = request.amount().multiply(BigDecimal.valueOf(100)).intValueExact();

        RazorpayPaymentLinkRequest paymentLinkRequest = RazorpayPaymentLinkRequest.builder()
                .amount(paise)
                .currency("INR")
                .description("Group Payment: " + request.groupId())
                .customer(new RazorpayPaymentLinkRequest.Customer(
                        "Member: " + request.memberId(),
                        null,
                        request.customerEmail()
                ))
                .notify(new RazorpayPaymentLinkRequest.Notify(true, true))
                .callbackUrl("https://localhost:8084/api/v1/payment/webhook")
                .callbackMethod("get")
                .build();

        RazorpayPaymentLinkResponse response = razorpayClient.createPaymentLink(paymentLinkRequest);

        newPayment.setProviderPaymentId(response.getId());
        newPayment.setHostedPaymentUrl(response.getShortUrl());
        newPayment.setUpdatedAt(Instant.now());

        return new PaymentIntentResponse(
                newPayment.getId().toString(),
                newPayment.getProviderPaymentId(),
                newPayment.getHostedPaymentUrl()
        );
    }

    @Transactional
    public void processWebhook(String payload, String signature) {
        if (!verifySignature(payload, signature)) {
            log.error("Invalid signature");
            return;
        }

        try {
            JsonNode root = new ObjectMapper().readTree(payload);

            String id = root.get("payload").get("payment_link").get("entity").get("id").asText();
            String status = root.get("payload").get("payment_link").get("entity").get("status").asText();

            Payment payment = paymentRepository.findByProviderPaymentId(id)
                    .orElseThrow();

            if ("active".equals(status)) return;

            if ("paid".equals(status)) {
                payment.setStatus(PaymentStatus.PAID);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
            }

            payment.setUpdatedAt(Instant.now());

        } catch (Exception e) {
            log.error("Webhook parse error: {}", e.getMessage());
        }
    }

    public boolean verifySignature(String payload, String signature) {
        try {
            String secret = razorpayProperties.getWebhookSecret();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));

            String computed = Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes()));

            return computed.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
