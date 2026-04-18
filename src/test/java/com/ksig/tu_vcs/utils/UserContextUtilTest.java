package com.ksig.tu_vcs.utils;

import com.ksig.tu_vcs.repos.AppUserRepository;
import com.ksig.tu_vcs.repos.entities.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UserContextUtilTest {

    private AppUserRepository repository;
    private UserContextUtil userContextUtil;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(AppUserRepository.class);
        userContextUtil = new UserContextUtil(repository);
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnCurrentUser() {
        UUID id = UUID.randomUUID();

        Jwt jwt = new Jwt(
                "token",
                null,
                null,
                Map.of("alg", "none"),
                Map.of("sub", id.toString())
        );

        JwtAuthenticationToken auth =
                new JwtAuthenticationToken(jwt);

        SecurityContextHolder.getContext()
                .setAuthentication(auth);

        AppUser user = new AppUser();

        when(repository.findById(id))
                .thenReturn(Optional.of(user));

        AppUser result = userContextUtil.getCurrentUser();

        assertNotNull(result);
        assertEquals(user, result);
    }

    @Test
    void shouldReturnNullWhenNoAuthentication() {
        AppUser result = userContextUtil.getCurrentUser();
        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenRepositoryEmpty() {
        UUID id = UUID.randomUUID();

        Jwt jwt = new Jwt(
                "token",
                null,
                null,
                Map.of("alg", "none"),
                Map.of("sub", id.toString())
        );

        JwtAuthenticationToken auth =
                new JwtAuthenticationToken(jwt);

        SecurityContextHolder.getContext()
                .setAuthentication(auth);

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        AppUser result = userContextUtil.getCurrentUser();

        assertNull(result);
    }
}