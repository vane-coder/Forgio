package com.forgio.controller;

import com.forgio.dto.request.NewsFeedRequest;
import com.forgio.dto.response.NewsFeedResponse;
import com.forgio.service.NewsFeedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/newsfeed")
@RequiredArgsConstructor
public class NewsFeedController {

    private final NewsFeedService newsFeedService;

    @GetMapping
    public ResponseEntity<List<NewsFeedResponse>> list() {
        return ResponseEntity.ok(newsFeedService.listPosts());
    }

    @PostMapping
    public ResponseEntity<NewsFeedResponse> create(@Valid @RequestBody NewsFeedRequest req) {
        return ResponseEntity.ok(newsFeedService.createPost(req));
    }
}