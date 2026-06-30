package com.forgio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    // This path lets the reports screen ask for weekly and monthly summaries
    @GetMapping
    public ResponseEntity<String> getWeeklyAndMonthlyReports() {
     return ResponseEntity.ok("Weekly and Monthly reports data fetched successfully!");

    }
}
