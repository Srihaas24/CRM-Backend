package com.crm.BackendApp.dto.response;

import java.time.LocalDateTime;

import com.crm.BackendApp.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeactivateUserResponse {
    private String message;
    private Long userId;
    private UserStatus status;
    private LocalDateTime deletedAt;
    private Long deactivatedBy;
    private String deactivationReason;
}
