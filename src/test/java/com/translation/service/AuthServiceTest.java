package com.translation.service;

import com.translation.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private final String USERNAME = "admin";
    private final String PASSWORD = "adminPass";
    private final String JWT_TOKEN = "test.jwt.token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "adminUsername", USERNAME);
        ReflectionTestUtils.setField(authService, "adminPassword", PASSWORD);
    }

    @Test
    void authenticate_Success() {
        when(jwtTokenProvider.createToken(anyString())).thenReturn(JWT_TOKEN);

        String token = authService.authenticate(USERNAME, PASSWORD);

        assertNotNull(token);
        assertEquals(JWT_TOKEN, token);
    }

    @Test
    void authenticate_InvalidUsername() {
        assertThrows(BadCredentialsException.class, () -> {
            authService.authenticate("wronguser", PASSWORD);
        });
    }

    @Test
    void authenticate_InvalidPassword() {
        assertThrows(BadCredentialsException.class, () -> {
            authService.authenticate(USERNAME, "wrongpass");
        });
    }

    @Test
    void authenticate_EmptyCredentials() {
        assertThrows(BadCredentialsException.class, () -> {
            authService.authenticate("", "");
        });
    }

    @Test
    void authenticate_NullCredentials() {
        assertThrows(BadCredentialsException.class, () -> {
            authService.authenticate(null, null);
        });
    }
}
