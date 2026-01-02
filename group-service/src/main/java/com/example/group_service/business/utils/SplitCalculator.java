package com.example.group_service.business.utils;

import com.example.group_service.business.dto.MemberRequest;
import com.example.group_service.data.entities.GroupMember;
import com.example.group_service.data.entities.GroupOrder;
import com.example.group_service.data.enums.MemberStatus;
import com.example.group_service.data.enums.SplitType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class SplitCalculator {

    public void applySplit(GroupOrder group, List<MemberRequest> members) {
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("Members are required");
        }

        if (group.getMembers() == null) {
            group.setMembers(new ArrayList<>());
        }

        BigDecimal totalAmount = group.getTotalAmount();

        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be greater than zero");
        }

        if (group.getSplitType() == SplitType.EQUAL) {
            applyEqualSplit(group, members, totalAmount);
        } else {
            applyCustomSplit(group, members, totalAmount);
        }

        group.setRequiredPaidAmount(totalAmount);
    }

    private void applyEqualSplit(
            GroupOrder group,
            List<MemberRequest> members,
            BigDecimal totalAmount
    ) {
        int size = members.size();

        BigDecimal baseAmount = totalAmount
                .divide(BigDecimal.valueOf(size), 2, RoundingMode.DOWN);

        BigDecimal allocated = BigDecimal.ZERO;

        for (int i = 0; i < size; i++) {
            MemberRequest req = members.get(i);

            BigDecimal amountToPay = (i == size - 1)
                    ? totalAmount.subtract(allocated)
                    : baseAmount;

            allocated = allocated.add(amountToPay);

            group.getMembers().add(
                    buildMember(req, group, amountToPay)
            );
        }
    }

    private void applyCustomSplit(
            GroupOrder group,
            List<MemberRequest> members,
            BigDecimal totalAmount
    ) {
        boolean missingAmount = members.stream()
                .anyMatch(m -> m.customAmount() == null);

        if (missingAmount) {
            throw new IllegalArgumentException(
                    "Custom amount is required for split type " + group.getSplitType()
            );
        }

        BigDecimal sum = members.stream()
                .map(MemberRequest::customAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(totalAmount) != 0) {
            throw new IllegalArgumentException(
                    "Sum of custom amounts must equal total amount"
            );
        }

        for (MemberRequest req : members) {
            group.getMembers().add(
                    buildMember(req, group, req.customAmount())
            );
        }
    }

    private GroupMember buildMember(
            MemberRequest req,
            GroupOrder group,
            BigDecimal amountToPay
    ) {
        if (amountToPay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        return GroupMember.builder()
                .groupOrder(group)
                .userId(req.userId())
                .email(req.email())
                .phone(req.phone())
                .amountToPay(amountToPay)
                .status(MemberStatus.INVITED)
                .invitedAt(Instant.now())
                .build();
    }
}