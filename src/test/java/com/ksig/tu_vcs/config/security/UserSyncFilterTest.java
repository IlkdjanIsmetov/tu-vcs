package com.ksig.tu_vcs.config.security;

import com.ksig.tu_vcs.repos.AppUserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;
import java.util.UUID;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ksig.tu_vcs.repos.entities.AppUser;

@ExtendWith(MockitoExtension.class)
class UserSyncFilterTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private UserSyncFilter userSyncFilter;

    @Test
    void shouldCreateAdminUserIfNotExists() throws Exception {

        UUID userId = UUID.randomUUID();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("sub")).thenReturn(userId.toString());
        when(jwt.getClaimAsString("preferred_username")).thenReturn("user");
        when(jwt.getClaimAsString("email")).thenReturn("email@test.com");

        Map<String, Object> realm = Map.of("roles", List.of("ADMIN"));
        when(jwt.getClaim("realm_access")).thenReturn(realm);

        JwtAuthenticationToken auth = mock(JwtAuthenticationToken.class);
        when(auth.getToken()).thenReturn(jwt);

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(appUserRepository.existsById(userId)).thenReturn(false);

        userSyncFilter.doFilterInternal(request, response, filterChain);

        verify(appUserRepository).save(any(AppUser.class));
        verify(filterChain).doFilter(request, response);
    }

}