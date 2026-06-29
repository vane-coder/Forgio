package com.forgio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/machines")
@RequiredArgsConstructor
public class MachineController {

    @GetMapping
    public ResponseEntity<String> listMachines() {
        return ResponseEntity.ok("Machines endpoint connected successfully!");
    }
}
