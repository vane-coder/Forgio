package com.forgio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/machines")
@RequiredArgsConstructor
public class MachineController {

    // This connects the backend server to the machines screen
    @GetMapping
    public ResponseEntity<String> getMachinesScreenData() {
        return ResponseEntity.ok("Machines screen backend connected successfully!");
    }
}
