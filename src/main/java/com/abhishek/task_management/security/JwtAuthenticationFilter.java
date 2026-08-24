package com.abhishek.task_management.security;

import com.abhishek.task_management.entity.User;
import com.abhishek.task_management.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JWTService jwtService, UserRepository userRepository){
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }

        String token = authHeader.substring(7);

        System.out.println("JWT received");

        try {
            String userIdString = jwtService.extractUserId(token);

            UUID userId = UUID.fromString(userIdString);

            User user = userRepository.findById(userId).orElse(null);

            if(user != null){
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        Collections.emptyList()
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
        }



        filterChain.doFilter(request,response);
    }
}
