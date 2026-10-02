package com.crm.BackendApp.dto.response;

import java.time.LocalDateTime;

import com.crm.BackendApp.enums.Role;
import com.crm.BackendApp.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private UserStatus status;
    private Long projectCount;

    // Admin-only fields (set to null for PM so Jackson drops them)
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private Integer failedLoginAttempts;
    private String invitedByEmail;

    private LocalDateTime deletedAt;
    private Long deactivatedBy;
    private String deactivationReason;
}
