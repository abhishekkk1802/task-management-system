package com.abhishek.task_management.dto;

import jakarta.validation.constraints.NotBlank;


public record UpdateProjectRequest(
        @NotBlank
        String name,

        String description
) {
}
