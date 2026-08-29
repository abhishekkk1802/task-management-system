package com.abhishek.task_management.service;

import com.abhishek.task_management.dto.*;
import com.abhishek.task_management.entity.User;
import com.abhishek.task_management.exception.ConflictException;
import com.abhishek.task_management.exception.EmailAlreadyExistsException;
import com.abhishek.task_management.exception.InvalidCredentialException;
import com.abhishek.task_management.repository.UserRepository;
import com.abhishek.task_management.security.JWTService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse getCurrentUser() {

        User currentUser = getCurrentUserEntity();

        return toResponse(currentUser);
    }

    public UserResponse register(RegisterRequest registerRequest){
        boolean emailAlreadyPresent = userRepository.existsByEmail(registerRequest.email());

        if(emailAlreadyPresent){
            throw new ConflictException("email already exists");
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

    public AuthResponse login(LoginRequest request){
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

        String token = jwtService.generateToken(user);

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        return new AuthResponse(
                userResponse,
                token
        );
    }

    @Transactional
    public UserResponse updateCurrentUser(
            UpdateProfileRequest request
    ) {

        User currentUser = getCurrentUserEntity();

        if (userRepository.existsByEmailAndIdNot(
                request.email(),
                currentUser.getId()
        )) {
            throw new ConflictException(
                    "Email already exists"
            );
        }
        currentUser.setName(request.name());
        currentUser.setEmail(request.email());

        User savedUser = userRepository.save(currentUser);

        return toResponse(savedUser);
    }

    private UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private User getCurrentUserEntity() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assert authentication != null;
        return (User) authentication.getPrincipal();
    }



}
