package com.crm.BackendApp.conroller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crm.BackendApp.dto.request.AcceptInviteRequest;
import com.crm.BackendApp.dto.request.InviteUserRequest;
import com.crm.BackendApp.dto.response.InviteUserResponse;
import com.crm.BackendApp.security.CustomUserDetails;
import com.crm.BackendApp.service.InvitationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InviteUserResponse> inviteUser(
            @Valid @RequestBody InviteUserRequest request,
            @AuthenticationPrincipal CustomUserDetails loggedInUser) {
        InviteUserResponse response = invitationService.inviteUser(request, loggedInUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/accept")
    public ResponseEntity<String> acceptInvitation(@Valid @RequestBody AcceptInviteRequest request) {
        invitationService.acceptInvitation(request);
        return ResponseEntity.ok("Invitation accepted successfully");
    }
}
