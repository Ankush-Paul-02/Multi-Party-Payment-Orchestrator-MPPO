package com.example.payment_service.data.repository;

import com.example.payment_service.data.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByProviderPaymentId(String paymentLinkId);
}

