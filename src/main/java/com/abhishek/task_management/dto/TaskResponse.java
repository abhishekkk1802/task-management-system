package com.abhishek.task_management.dto;

import com.abhishek.task_management.entity.TaskPriority;
import com.abhishek.task_management.entity.TaskStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDateTime dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UUID createdBy,
        UUID assignedTo,
        UUID projectId
) {


}