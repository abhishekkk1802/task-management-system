package com.abhishek.task_management.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        UUID createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
