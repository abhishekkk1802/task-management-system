package com.abhishek.task_management.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTaskRequest(

        @NotBlank
        String title,

        String description,

        LocalDateTime dueDate,

        UUID assignedTo,

        UUID projectId

) {
}