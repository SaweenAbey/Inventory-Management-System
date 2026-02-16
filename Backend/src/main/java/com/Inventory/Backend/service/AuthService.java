package com.Inventory.Backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.Inventory.Backend.dto.RegisterRequest;
import com.Inventory.Backend.entity.Role;
import com.Inventory.Backend.entity.User;
import com.Inventory.Backend.repository.UserRepository;
import com.Inventory.Backend.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String register(RegisterRequest registerRequest) {
        logger.debug("Starting user registration for username: {}", registerRequest.getUsername());
        
        if(userRepository.existsByUsername(registerRequest.getUsername())){
            logger.warn("Registration failed: Username already exists - {}", registerRequest.getUsername());
            throw new RuntimeException("Username already exists");
        }

        Role assignedRole = registerRequest.getRole() != null ? registerRequest.getRole() : Role.USER;
        logger.debug("Assigning role: {} to user: {}", assignedRole, registerRequest.getUsername());
        
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .imageUrl(registerRequest.getImageUrl())
                .role(assignedRole)
                .build();

        logger.debug("Saving user to database: {}", user.getUsername());
        userRepository.save(user);
        logger.info("User registered successfully: {} with email: {} and role: {}", user.getUsername(), user.getEmail(), user.getRole());

        return "User Registered Successfully";
    }

    public String login(String username, String password) {
        logger.debug("Authenticating user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Login failed: User not found - {}", username);
                    return new RuntimeException("User not found");
                });

        if(!passwordEncoder.matches(password, user.getPassword())){
            logger.warn("Login failed: Invalid credentials for user - {}", username);
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(username);
        logger.info("User authenticated successfully: {}", username);
        return token;
    }

}
