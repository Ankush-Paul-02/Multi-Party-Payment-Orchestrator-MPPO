package com.example.group_service.data.entities;

import com.example.group_service.data.enums.MemberStatus;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "group_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;   // links to User.id (String)

    private String email;
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_order_id")
    private GroupOrder groupOrder;

    private BigDecimal amountToPay;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    private String paymentId;  // internal PAYMENT-SERVICE id

    private Instant invitedAt;
    private Instant paidAt;
}
