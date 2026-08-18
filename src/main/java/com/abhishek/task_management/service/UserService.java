package com.abhishek.task_management.service;

import com.abhishek.task_management.dto.LoginRequest;
import com.abhishek.task_management.dto.RegisterRequest;
import com.abhishek.task_management.dto.UserResponse;
import com.abhishek.task_management.entity.User;
import com.abhishek.task_management.exception.EmailAlreadyExistsException;
import com.abhishek.task_management.exception.InvalidCredentialException;
import com.abhishek.task_management.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest registerRequest){
        boolean emailAlreadyPresent = userRepository.existsByEmail(registerRequest.email());

        if(emailAlreadyPresent){
            throw new EmailAlreadyExistsException("email already exists");
        }

        String encodedPassword = passwordEncoder.encode(registerRequest.password());
        User savedUser = new User(registerRequest.name(),registerRequest.email(),encodedPassword);
        userRepository.save(savedUser);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt()
        );
    }

    public UserResponse login(LoginRequest request){
        User user = userRepository
                .findByEmail(request.email())
                .orElseThrow(()->new InvalidCredentialException(
                        "Invalid email or password"
                )
                );

        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPasswordHash());

        if(!passwordMatches){
            throw new InvalidCredentialException(
                    "Invalid email or password"
            );
        }

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }



}
