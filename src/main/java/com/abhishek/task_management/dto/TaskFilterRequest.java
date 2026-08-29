package com.abhishek.task_management.dto;

import com.abhishek.task_management.entity.TaskPriority;
import com.abhishek.task_management.entity.TaskStatus;

import java.util.UUID;

public record TaskFilterRequest(
        TaskStatus status,
        TaskPriority priority,
        UUID projectId,
        UUID assignedTo,
        String search,
        String sortBy,
        String sortDirection
) {
}