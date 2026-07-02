package com.forgio.controller;

import com.forgio.dto.response.AiSuggestionResponse;
import com.forgio.service.AiAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    @GetMapping("/suggestions")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AiSuggestionResponse> getSuggestions() {
        return ResponseEntity.ok(aiAssistantService.getSuggestion());
    }

    @PostMapping("/refresh")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AiSuggestionResponse> refresh() {
        return ResponseEntity.ok(aiAssistantService.getSuggestion());
    }
}