package com.Inventory.Backend.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.Inventory.Backend.entity.User;
import com.Inventory.Backend.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${jwt.secret:z7x9c5v3b1n8m0q2w4r6t8y0u2i4o6p8z7x9c5v3b1n8m0q2w4r6t8y0u2i4o6p8z7x9c5v3b1}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:3600000}")
    private long jwtExpiration;
    
    private SecretKey signingKey;
    
    @PostConstruct
    public void initializeSigningKey() {
        try {
            byte[] secretBytes = jwtSecret.getBytes();
            // Ensure the key is at least 512 bits (64 bytes) for HS512
            if (secretBytes.length < 64) {
                logger.warn("JWT secret is too short ({} bytes). It must be at least 64 bytes for HS512. Using generated secure key instead.", secretBytes.length);
                signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            } else {
                signingKey = Keys.hmacShaKeyFor(secretBytes);
            }
            logger.info("JWT signing key initialized with {} bits", signingKey.getEncoded().length * 8);
        } catch (Exception e) {
            logger.error("Error initializing JWT signing key", e);
            signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        }
    }

    public String register(User user) {

        if(userRepository.existsByUsername(user.getUsername())){
            throw new RuntimeException("Username already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return "User Registered Successfully";
    }

    public String login(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new RuntimeException("Invalid credentials");
        }

        return generateToken(username);
    }
    
    public String generateToken(String username) {
        logger.debug("Generating JWT token for username: {}", username);
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
        logger.debug("JWT token generated successfully for username: {}", username);
        return token;
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            logger.debug("JWT token validation successful");
            return true;
        } catch (Exception e) {
            logger.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    public String getUsernameFromToken(String token) {
        logger.debug("Extracting username from JWT token");
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
