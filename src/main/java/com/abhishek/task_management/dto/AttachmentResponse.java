package com.abhishek.task_management.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AttachmentResponse(
        UUID id,
        String fileName,
        String contentType,
        Long fileSize,
        UUID taskId,
        UUID uploadedBy,
        LocalDateTime createdAt
) {
}