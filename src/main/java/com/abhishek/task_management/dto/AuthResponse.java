package com.abhishek.task_management.dto;

public record AuthResponse (
        UserResponse user,
        String token
){
}
