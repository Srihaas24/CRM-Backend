package com.crm.BackendApp.conroller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crm.BackendApp.dto.request.DeactivateUserRequest;
import com.crm.BackendApp.dto.request.UploadUserRequest;
import com.crm.BackendApp.dto.response.DeactivateUserResponse;
import com.crm.BackendApp.dto.response.UploadUserResponse;
import com.crm.BackendApp.dto.response.UserResponse;
import com.crm.BackendApp.enums.UserStatus;
import com.crm.BackendApp.security.CustomUserDetails;
import com.crm.BackendApp.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@PostMapping({"", "/upload", "/uploaduser"})
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<UploadUserResponse> uploadusers(@RequestBody UploadUserRequest uploadUserRequest,
			Authentication authentication) {
		CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(userService.saveUser(uploadUserRequest, customUserDetails));
	}

	@PatchMapping("/{userId}/deactivate")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<DeactivateUserResponse> deactivateUser(
			@PathVariable Long userId,
			@RequestBody(required = false) DeactivateUserRequest request,
			@RequestParam(required = false) String reason,
			@AuthenticationPrincipal CustomUserDetails currentUser,
			HttpServletRequest httpRequest) {

		String resolvedReason = (request != null && request.getReason() != null && !request.getReason().isBlank())
				? request.getReason()
				: reason;
		String ipAddress = httpRequest.getRemoteAddr();

		DeactivateUserResponse response = userService.deactivateUser(userId, resolvedReason, currentUser, ipAddress);
		return ResponseEntity.ok(response);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
	public ResponseEntity<Page<UserResponse>> listTenantUsers(
			@AuthenticationPrincipal CustomUserDetails currentUser,
			@RequestParam(required = false) UserStatus status,
			@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

		Page<UserResponse> users = userService.getTenantUsers(currentUser.getUser(), status, pageable);
		return ResponseEntity.ok(users);
	}

}
