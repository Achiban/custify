package com.custify.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Test
    void generateTokenShouldBeReadableAndValid() {
        String token = jwtUtil.generateToken("alice@mail.com");

        assertNotNull(token);
        assertEquals("alice@mail.com", jwtUtil.extractUsername(token));
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateTokenShouldReturnFalseForInvalidToken() {
        assertFalse(jwtUtil.validateToken("not-a-token"));
    }
}
