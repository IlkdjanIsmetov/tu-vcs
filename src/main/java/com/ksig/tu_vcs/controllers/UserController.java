package com.ksig.tu_vcs.controllers;

import com.ksig.tu_vcs.repos.AppUserRepository;
import com.ksig.tu_vcs.repos.entities.AppUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final AppUserRepository appUserRepository;

    public UserController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    // Returns a safe public view of all users (no sensitive data)
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getAllUsers() {
        List<Map<String, String>> users = appUserRepository.findAll()
                .stream()
                .map(u -> Map.of(
                        "username",   u.getUsername(),
                        "email",      u.getEmail() != null ? u.getEmail() : "",
                        "systemRole", u.getSystemRole().name(),
                        "joinedAt",   u.getCreatedAt() != null ? u.getCreatedAt().toString() : ""
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }
}
