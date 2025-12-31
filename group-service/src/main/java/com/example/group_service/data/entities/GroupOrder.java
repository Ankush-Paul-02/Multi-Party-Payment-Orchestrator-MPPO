package com.example.group_service.data.entities;

import com.example.group_service.data.enums.GroupStatus;
import com.example.group_service.data.enums.SplitType;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // your User.id is String UUID
    private String hostUserId;

    private String externalCartId;

    @Enumerated(EnumType.STRING)
    private GroupStatus status;

    @Enumerated(EnumType.STRING)
    private SplitType splitType;

    private BigDecimal totalAmount;
    private BigDecimal requiredPaidAmount;

    private Instant expiresAt;

    @Builder.Default
    @OneToMany(mappedBy = "groupOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMember> members = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;
}

