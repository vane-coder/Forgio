package com.forgio.controller;

import com.forgio.dto.request.ChangePasswordRequest;
import com.forgio.dto.request.UpdateProfileRequest;
import com.forgio.dto.response.ProfileResponse;
import com.forgio.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile() {
        return ResponseEntity.ok(profileService.getMyProfile());
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(profileService.updateMyProfile(req));
    }

    @PostMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        profileService.changePassword(req);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
}