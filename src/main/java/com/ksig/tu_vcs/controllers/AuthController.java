package com.ksig.tu_vcs.controllers;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final String serverUrl = "http://localhost:8081";
    private final String realm     = "vcs-realm";

    private Keycloak adminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .username("admin")
                .password("admin")
                .clientId("admin-cli")
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            UserRepresentation user = new UserRepresentation();
            user.setEnabled(true);
            user.setUsername(body.get("username"));
            user.setEmail(body.get("email"));
            user.setFirstName(body.get("firstName"));
            user.setLastName(body.get("lastName"));
            user.setEmailVerified(true);
            user.setRequiredActions(Collections.emptyList());

            CredentialRepresentation password = new CredentialRepresentation();
            password.setTemporary(false);
            password.setType(CredentialRepresentation.PASSWORD);
            password.setValue(body.get("password"));
            user.setCredentials(List.of(password));

            Response response = adminClient().realm(realm).users().create(user);

            if (response.getStatus() == 201) {
                return ResponseEntity.ok(Map.of("message", "Registration successful."));
            } else if (response.getStatus() == 409) {
                return ResponseEntity.status(409).body(Map.of("message", "Username or email already exists."));
            } else {
                return ResponseEntity.status(response.getStatus()).body(Map.of("message", "Registration failed."));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> body) {
        try {
            String userId = jwt.getClaimAsString("sub");

            UserRepresentation user = adminClient().realm(realm).users().get(userId).toRepresentation();
            if (body.containsKey("firstName")) user.setFirstName(body.get("firstName"));
            if (body.containsKey("lastName"))  user.setLastName(body.get("lastName"));
            if (body.containsKey("email")) {
                user.setEmail(body.get("email"));
                user.setEmailVerified(true);
            }

            adminClient().realm(realm).users().get(userId).update(user);
            return ResponseEntity.ok(Map.of("message", "Profile updated successfully."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        System.out.println("Password reset requested for: " + email);
        return ResponseEntity.ok(Map.of("message", "If the email exists, reset instructions have been sent."));
    }
}
