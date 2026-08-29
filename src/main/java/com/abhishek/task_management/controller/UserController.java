package com.abhishek.task_management.controller;

import com.abhishek.task_management.dto.UpdateProfileRequest;
import com.abhishek.task_management.dto.UserResponse;
import com.abhishek.task_management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        return userService.getCurrentUser();
    }

    @PutMapping("/me")
    public UserResponse updateCurrentUser(
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return userService.updateCurrentUser(request);
    }
}