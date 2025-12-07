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
import java.util.List;

@Component
public class SplitCalculator {

    public void applySplit(GroupOrder group, List<MemberRequest> members) {
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("Members are required");
        }

        if (group.getSplitType() == SplitType.EQUAL) {
            BigDecimal part = group.getTotalAmount()
                    .divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);

            for (MemberRequest req : members) {
                group.getMembers().add(buildMember(req, group, part));
            }
        } else {
            BigDecimal sum = members.stream()
                    .map(MemberRequest::customAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (sum.compareTo(group.getTotalAmount()) != 0) {
                throw new IllegalArgumentException("Custom amounts must equal total");
            }

            for (MemberRequest req : members) {
                group.getMembers().add(buildMember(req, group, req.customAmount()));
            }
        }

        group.setRequiredPaidAmount(group.getTotalAmount());
    }

    private GroupMember buildMember(MemberRequest req, GroupOrder group, BigDecimal amountToPay) {
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

