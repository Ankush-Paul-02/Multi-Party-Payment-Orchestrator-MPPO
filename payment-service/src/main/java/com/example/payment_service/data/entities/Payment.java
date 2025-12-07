package com.example.payment_service.data.entities;

import com.example.payment_service.data.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long groupId;
    private Long memberId;

    private BigDecimal amount;

    private String provider;
    private String providerPaymentId;
    private String hostedPaymentUrl;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Instant createdAt;
    private Instant updatedAt;
}
