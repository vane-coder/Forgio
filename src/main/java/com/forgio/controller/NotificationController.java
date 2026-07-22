package com.forgio.controller;

import com.forgio.dto.request.NotificationRequest;
import com.forgio.dto.response.NotificationResponse;
import com.forgio.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list() {
        return ResponseEntity.ok(notificationService.listNotifications());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','DEPT_HEAD')")
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody NotificationRequest req) {
        return ResponseEntity.ok(notificationService.send(req));
    }
    @GetMapping("/sent")
    public ResponseEntity<List<NotificationResponse>> sent() {
        return ResponseEntity.ok(notificationService.listSentByMe());
    }
}