package com.ksig.tu_vcs.controllers;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final String serverUrl = "http://localhost:8081";
    private final String realm = "vcs-realm";

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            // Свързваме се с Keycloak като администратор
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm("master")
                    .username("admin")
                    .password("admin")
                    .clientId("admin-cli")
                    .build();

            // Подготвяме данните за новия потребител
            UserRepresentation user = new UserRepresentation();
            user.setEnabled(true);
            user.setUsername(body.get("username"));
            user.setEmail(body.get("email"));
            user.setFirstName(body.get("firstName"));
            user.setLastName(body.get("lastName"));
            user.setEmailVerified(true);
            user.setRequiredActions(Collections.emptyList());

            // Настройваме паролата
            CredentialRepresentation password = new CredentialRepresentation();
            password.setTemporary(false);
            password.setType(CredentialRepresentation.PASSWORD);
            password.setValue(body.get("password"));
            user.setCredentials(Collections.singletonList(password));

            // Изпращаме към Keycloak
            Response response = keycloak.realm(realm).users().create(user);

            if (response.getStatus() == 201) {
                return ResponseEntity.ok(Map.of("message", "Success"));
            } else if (response.getStatus() == 409) {
                return ResponseEntity.status(409).body(
                        Map.of("message", "Username or email already exists.")
                );
            } else {
                return ResponseEntity.status(response.getStatus()).body(
                        Map.of("message", "Registration failed. Please try again.")
                );
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("message", "Error: " + e.getMessage())
            );
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        System.out.println("Password reset request for: " + email);
        return ResponseEntity.ok(Map.of("message", "If the email exists, instructions have been sent."));
    }
}
