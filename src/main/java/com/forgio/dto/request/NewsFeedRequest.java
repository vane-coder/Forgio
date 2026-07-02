package com.forgio.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NewsFeedRequest(
        @NotBlank(message = "Post content cannot be empty") String content
) {}