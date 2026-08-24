package com.abhishek.task_management.controller;

import com.abhishek.task_management.dto.AuthResponse;
import com.abhishek.task_management.dto.LoginRequest;
import com.abhishek.task_management.dto.RegisterRequest;
import com.abhishek.task_management.dto.UserResponse;
import com.abhishek.task_management.entity.User;
import com.abhishek.task_management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest registerRequest){
       return userService.register(registerRequest);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest loginRequest
            ){
        return userService.login(loginRequest);
    }

}
