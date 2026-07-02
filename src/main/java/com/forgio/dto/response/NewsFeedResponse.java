package com.forgio.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NewsFeedResponse(
        UUID postId,
        String content,
        UUID authorId,
        String authorName,
        Instant createdAt
) {}