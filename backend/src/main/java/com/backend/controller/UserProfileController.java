package com.backend.controller;

import com.backend.dto.auth.ProfileUpdateRequest;
import com.backend.dto.auth.UserProfileDto;
import com.backend.security.CustomUserDetails;
import com.backend.service.AuthUserService;
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

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> me(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(authUserService.getProfile(principal.getId()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileDto> updateMe(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(authUserService.updateProfile(principal.getId(), request));
    }
}
