package com.translation.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String SECRET = "test-secret-key-that-is-at-least-32-characters-long";
    private static final long EXPIRATION = 3600000; // 1 hour
    private static final String USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", EXPIRATION);
        jwtTokenProvider.init();
    }

    @Test
    void createToken_Success() {
        String token = jwtTokenProvider.createToken(USERNAME);

        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_InvalidToken() {
        String invalidToken = "invalid.token.here";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void validateToken_NullToken() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    void getAuthentication_Success() {
        String token = jwtTokenProvider.createToken(USERNAME);
        Authentication auth = jwtTokenProvider.getAuthentication(token);

        assertNotNull(auth);
        assertEquals(USERNAME, auth.getName());
    }

    @Test
    void getAuthentication_InvalidToken() {
        String invalidToken = "invalid.token.here";
        assertThrows(JwtException.class, () -> {
            jwtTokenProvider.getAuthentication(invalidToken);
        });
    }

    @Test
    void getUsername_Success() {
        String token = jwtTokenProvider.createToken(USERNAME);
        String extractedUsername = jwtTokenProvider.getUsername(token);

        assertEquals(USERNAME, extractedUsername);
    }

    @Test
    void tokenExpiration_Success() {
        String token = jwtTokenProvider.createToken(USERNAME);
        
        // Token should be valid now
        assertTrue(jwtTokenProvider.validateToken(token));
        
        // Wait for a small amount of time to ensure we're not at the exact expiration time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Token should still be valid as it hasn't expired
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void createMultipleTokens_Success() {
        String token1 = jwtTokenProvider.createToken(USERNAME);
        String token2 = jwtTokenProvider.createToken(USERNAME);

        assertNotEquals(token1, token2);
        assertTrue(jwtTokenProvider.validateToken(token1));
        assertTrue(jwtTokenProvider.validateToken(token2));
    }
}
