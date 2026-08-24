package com.abhishek.task_management.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateProjectRequest(
        @NotBlank
        String name,

        String description
) {
}
