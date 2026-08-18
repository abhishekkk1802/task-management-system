package com.abhishek.task_management.dto;

import java.util.Map;

public record ErrorResponse(
        int status,
        String message,
        Map<String,String> errors
) {
}
