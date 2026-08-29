package com.abhishek.task_management.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        String content,
        UUID taskId,
        UUID createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}