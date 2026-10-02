package com.crm.BackendApp.dto.response;

import com.crm.BackendApp.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteUserResponse {
    private Long id;
    private String email;
    private Role role;
    private String token;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
