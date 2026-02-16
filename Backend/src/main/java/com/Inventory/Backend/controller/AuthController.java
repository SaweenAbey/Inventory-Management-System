package com.Inventory.Backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Inventory.Backend.dto.LoginRequest;
import com.Inventory.Backend.dto.RegisterRequest;
import com.Inventory.Backend.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest registerRequest){
        logger.info("Registration attempt for username: {}", registerRequest.getUsername());
        try {
            String result = authService.register(registerRequest);
            logger.info("User registered successfully: {}", registerRequest.getUsername());
            return result;
        } catch (Exception e) {
            logger.error("Registration failed for username: {}", registerRequest.getUsername(), e);
            throw e;
        }
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest){
        logger.info("Login attempt for username: {}", loginRequest.getUsername());
        try {
            String result = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
            logger.info("User logged in successfully: {}", loginRequest.getUsername());
            return result;
        } catch (Exception e) {
            logger.error("Login failed for username: {}", loginRequest.getUsername(), e);
            throw e;
        }
    }

}
