package com.ksig.tu_vcs.config.security;

import com.ksig.tu_vcs.repos.AppUserRepository;
import com.ksig.tu_vcs.repos.entities.AppUser;
import com.ksig.tu_vcs.repos.entities.enums.SystemRole;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            List<String> roles = realmAccess != null
                    ? (List<String>) realmAccess.get("roles")
                    : Collections.emptyList();

            if (!appUserRepository.existsById(userId)) {
                AppUser newUser = new AppUser();
                newUser.setId(userId);
                newUser.setUsername(jwt.getClaimAsString("preferred_username"));
                newUser.setEmail(jwt.getClaimAsString("email"));
                if (roles != null && roles.contains("admin")) {
                    newUser.setSystemRole(SystemRole.ADMIN);
                } else {
                    newUser.setSystemRole(SystemRole.USER);
                }
                appUserRepository.save(newUser);
            }
        }

        filterChain.doFilter(request, response);
    }
}