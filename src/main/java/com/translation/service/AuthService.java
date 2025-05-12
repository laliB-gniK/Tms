package com.translation.service;

import com.translation.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${auth.admin.username}")
    private String adminUsername;

    @Value("${auth.admin.password}")
    private String adminPassword;

    public String authenticate(String username, String password) {
        // For simplicity, we're using a single admin user
        // In a production environment, you would want to use proper user management
        if (adminUsername.equals(username) && adminPassword.equals(password)) {
            log.info("User authenticated successfully: {}", username);
            return jwtTokenProvider.createToken(username);
        }
        
        log.warn("Authentication failed for user: {}", username);
        throw new BadCredentialsException("Invalid credentials");
    }
}
