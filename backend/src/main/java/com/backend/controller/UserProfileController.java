package com.backend.controller;

import com.backend.dto.auth.ProfileUpdateRequest;
import com.backend.dto.auth.UserProfileDto;
import com.backend.security.CustomUserDetails;
import com.backend.service.AuthUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserProfileController {

    private final AuthUserService authUserService;

    public UserProfileController(AuthUserService authUserService) {
        this.authUserService = authUserService;
    }

    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileDto> profile(@AuthenticationPrincipal(expression = "id") Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authUserService.getProfile(userId));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<UserProfileDto> updateProfile(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody(required = false) ProfileUpdateRequest request) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ProfileUpdateRequest safeRequest = request != null ? request : new ProfileUpdateRequest();
        return ResponseEntity.ok(authUserService.updateProfile(userId, safeRequest));
    }
}
