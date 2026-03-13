package com.ksig.tu_vcs.config.security;

import com.ksig.tu_vcs.repos.AppUserRepository;
import com.ksig.tu_vcs.repos.entities.AppUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class UserSyncFilter extends OncePerRequestFilter {

    private final AppUserRepository appUserRepository;

    public UserSyncFilter(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthToken) {
            Jwt jwt = jwtAuthToken.getToken();
            UUID userId = UUID.fromString(jwt.getClaimAsString("sub"));

            if (!appUserRepository.existsById(userId)) {
                AppUser newUser = new AppUser();
                newUser.setId(userId);
                newUser.setUsername(jwt.getClaimAsString("preferred_username"));
                newUser.setEmail(jwt.getClaimAsString("email"));
                
                appUserRepository.save(newUser);
            }
        }

        filterChain.doFilter(request, response);
    }
}