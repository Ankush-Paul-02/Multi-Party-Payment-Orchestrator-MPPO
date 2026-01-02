package com.example.group_service.business.service;

import com.example.group_service.business.dto.CreateGroupRequest;
import com.example.group_service.business.dto.GroupResponse;
import com.example.group_service.business.dto.MemberRequest;
import com.example.group_service.business.dto.MemberResponse;
import com.example.group_service.business.shared.CreatePaymentIntentRequest;
import com.example.group_service.business.shared.PaymentIntentResponse;
import com.example.group_service.business.utils.SplitCalculator;
import com.example.group_service.client.PaymentClient;
import com.example.group_service.client.UserClient;
import com.example.group_service.data.entities.GroupMember;
import com.example.group_service.data.entities.GroupOrder;
import com.example.group_service.data.enums.GroupStatus;
import com.example.group_service.data.enums.MemberStatus;
import com.example.group_service.data.repositories.GroupOrderRepository;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupOrderRepository groupOrderRepository;
    private final SplitCalculator splitCalculator;
    private final PaymentClient paymentClient;
    private final UserClient userClient;

    @Transactional
    public GroupResponse createGroup(String hostUserId, CreateGroupRequest request) {
        ///  If split type is not equal then custom amount is required in {@link MemberRequest}
        ///  check the group-members are valid or not
        List<MemberRequest> memberRequests = validateAndAttachUserIds(request.members());

        ///  Build the group order
        GroupOrder groupOrder = GroupOrder.builder()
                .hostUserId(hostUserId)
                .externalCartId(request.cartId())
                .totalAmount(request.totalAmount())
                .splitType(request.splitType())
                .status(GroupStatus.DRAFT)
                .expiresAt(Instant.now().plusSeconds(600))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        splitCalculator.applySplit(groupOrder, memberRequests);

        groupOrder.setStatus(GroupStatus.OPEN);
        groupOrderRepository.saveAndFlush(groupOrder);

        // create payment intents for all members
        for (GroupMember groupMember: groupOrder.getMembers()) {
            PaymentIntentResponse response = paymentClient.createIntent(
                    new CreatePaymentIntentRequest(
                            groupOrder.getId(),
                            groupMember.getId(),
                            groupMember.getAmountToPay(),
                            groupMember.getEmail()
                    )
            );
            groupMember.setPaymentId(response.paymentId());
        }
        groupOrderRepository.save(groupOrder);
        return toResponse(groupOrder);
    }

    @Transactional
    public void markMemberPaid(Long groupId, Long memberId) {
        Optional<GroupOrder> optionalGroupOrder = groupOrderRepository.findByIdForUpdate(groupId);
        if (optionalGroupOrder.isEmpty()) {
            throw new RuntimeException("Invalid groupId!");
        }
        GroupOrder groupOrder = optionalGroupOrder.get();

        GroupMember groupMember = groupOrder.getMembers().stream()
                .filter(m -> m.getId().equals(memberId))
                .findFirst()
                .orElseThrow();

        if (groupMember.getStatus().equals(MemberStatus.PAID)) {
            return;
        }

        groupMember.setStatus(MemberStatus.PAID);
        groupMember.setPaidAt(Instant.now());

        boolean hasAllPaid = groupOrder.getMembers().stream()
                .allMatch(m -> m.getStatus().equals(MemberStatus.PAID));

        if (hasAllPaid) {
            groupOrder.setStatus(GroupStatus.PENDING);
            // here you would publish group.finalized event to ORDER-SERVICE

        }
    }

    public GroupResponse getGroup(Long groupId) {
        Optional<GroupOrder> optionalGroupOrder = groupOrderRepository.findById(groupId);
        if (optionalGroupOrder.isEmpty()) {
            throw new RuntimeException("Invalid groupId!");
        }

        return toResponse(optionalGroupOrder.get());
    }

    private GroupResponse toResponse(GroupOrder g) {
        List<MemberResponse> members = g.getMembers().stream()
                .map(m -> new MemberResponse(
                        m.getId(),
                        m.getUserId(),
                        m.getEmail(),
                        m.getAmountToPay(),
                        m.getStatus()
                ))
                .toList();

        return new GroupResponse(
                g.getId(),
                g.getHostUserId(),
                g.getStatus(),
                g.getSplitType(),
                g.getTotalAmount(),
                g.getRequiredPaidAmount(),
                members
        );
    }

    private List<MemberRequest> validateAndAttachUserIds(
            List<MemberRequest> memberRequests
    ) {
        return memberRequests.stream()
                .map(member -> {
                    try {
                        String userId = userClient.getUserIdByEmail(member.email());

                        return new MemberRequest(
                                userId,
                                member.email(),
                                member.phone(),
                                member.customAmount()
                        );

                    } catch (FeignException.NotFound e) {
                        throw new RuntimeException(
                                "User not registered: " + member.email()
                        );
                    } catch (FeignException e) {
                        throw new RuntimeException(
                                "User service unavailable. Please try again."
                        );
                    }
                })
                .toList();
    }
}
