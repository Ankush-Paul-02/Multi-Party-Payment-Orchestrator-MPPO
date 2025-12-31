package com.example.group_service.controller;

import com.example.group_service.business.dto.CreateGroupRequest;
import com.example.group_service.business.dto.GroupResponse;
import com.example.group_service.business.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping("/hi")
    public String sayHi(Principal principal) {
        return "Hi " + principal.getName() + ", Group Service is running...";
    }

    @PostMapping("/create")
    public ResponseEntity<GroupResponse> createGroup(
            Principal principal,
            @RequestBody @Valid CreateGroupRequest request
    ) {
        String hostUserId = principal.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createGroup(hostUserId, request));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(
            @PathVariable Long groupId
    ) {
        return ResponseEntity.ok(groupService.getGroup(groupId));
    }

    @PostMapping("/{groupId}/members/{memberId}/paid")
    public ResponseEntity<Void> markPaid(
            @PathVariable Long groupId,
            @PathVariable Long memberId
    ) {
        groupService.markMemberPaid(groupId, memberId);
        return ResponseEntity.ok().build();
    }
}
