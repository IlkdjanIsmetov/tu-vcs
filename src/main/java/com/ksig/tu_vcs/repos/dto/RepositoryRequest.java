package com.ksig.tu_vcs.repos.dto;

import java.util.UUID;

public record RepositoryRequest(
        String name,
        String description,
        UUID ownerId
) {
}
