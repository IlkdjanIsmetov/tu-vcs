package com.ksig.tu_vcs.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    @Test
    void shouldExtractRoles() {

        SecurityConfig config = new SecurityConfig(null);

        Jwt jwt = mock(Jwt.class);

        Map<String, Object> realm = Map.of("roles", List.of("admin", "user"));
        when(jwt.getClaim("realm_access")).thenReturn(realm);

        var converter = config.jwtAuthenticationConverter();

        var authorities = converter.convert(jwt).getAuthorities();

        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void shouldReturnEmptyWhenNoRoles() {

        SecurityConfig config = new SecurityConfig(null);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("realm_access")).thenReturn(null);

        var converter = config.jwtAuthenticationConverter();

        var authorities = converter.convert(jwt).getAuthorities();

        assertTrue(authorities.stream()
                .noneMatch(a -> a.getAuthority().startsWith("ROLE_")));
    }
}