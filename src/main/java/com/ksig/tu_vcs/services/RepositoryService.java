package com.ksig.tu_vcs.services;

import com.ksig.tu_vcs.repos.RepositoryMemberRepository;
import com.ksig.tu_vcs.repos.RepositoryRepository;
import com.ksig.tu_vcs.repos.entities.Repository;
import com.ksig.tu_vcs.repos.entities.RepositoryMember;
import org.springframework.stereotype.Service;


import java.util.UUID;

@Service
public class RepositoryService {
    private final RepositoryRepository repositoryRepository;
    private final RepositoryMemberRepository repositoryMemberRepository;


    public RepositoryService(RepositoryRepository repositoryRepository, RepositoryMemberRepository repositoryMemberRepository) {
        this.repositoryRepository = repositoryRepository;
        this.repositoryMemberRepository = repositoryMemberRepository;
    }

    public Repository createRepository(String name, String description, UUID owner) {
        if (repositoryRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Repository name already exists");
        }

        Repository repository = new Repository();
        repository.setName(name);
        repository.setDescription(description);
        repository.setOwnerID(owner);
        repository.setRequiresApprovalByDefault(true);

        repositoryRepository.save(repository);

        RepositoryMember member = new RepositoryMember();
        member.setRepositoryId(repository.getId());
        member.setUserID(owner);
        member.setRole("MASTER");

        repositoryMemberRepository.save(member);

        return repository;


    }
}
