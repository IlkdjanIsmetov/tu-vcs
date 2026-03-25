package com.ksig.tu_vcs.services;


import com.ksig.tu_vcs.repos.ItemRepository;
import com.ksig.tu_vcs.repos.ItemRevisionRepository;
import com.ksig.tu_vcs.repos.RepositoryMemberRepository;
import com.ksig.tu_vcs.repos.RepositoryRepository;
import com.ksig.tu_vcs.repos.RevisionRepository;
import com.ksig.tu_vcs.repos.entities.*;
import com.ksig.tu_vcs.repos.entities.enums.Action;
import com.ksig.tu_vcs.repos.entities.enums.ItemType;
import com.ksig.tu_vcs.repos.entities.enums.Role;
import com.ksig.tu_vcs.services.exceptions.CommitException;
import com.ksig.tu_vcs.services.views.ItemInView;
import com.ksig.tu_vcs.services.views.ItemOutView;
import com.ksig.tu_vcs.services.views.RepositoryInView;
import com.ksig.tu_vcs.services.views.RepositoryOutView;
import com.ksig.tu_vcs.utils.UserContextUtil;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class RepositoryService {


    private final RepositoryRepository repositoryRepository;
    private final RepositoryMemberRepository repositoryMemberRepository;
    private final UserContextUtil userContextUtil;
    private final ItemRevisionRepository itemRevisionRepository;
    private final CommitService commitService;


    public RepositoryService(RepositoryRepository repositoryRepository, RepositoryMemberRepository repositoryMemberRepository,
                             UserContextUtil userContextUtil,
                             ItemRevisionRepository itemRevisionRepository, CommitService commitService) {
        this.repositoryRepository = repositoryRepository;
        this.repositoryMemberRepository = repositoryMemberRepository;
        this.userContextUtil = userContextUtil;
        this.itemRevisionRepository = itemRevisionRepository;
        this.commitService = commitService;
    }

    @Transactional
    public RepositoryOutView createRepository(RepositoryInView view) {
        AppUser currentUser = userContextUtil.getCurrentUser();
        Repository repository = new Repository();
        repository.setName(view.getRepositoryName());
        repository.setDescription(view.getDescription());
        repository.setOwner(currentUser);
        repository.setRequiresApprovalByDefault(true);

        repository = repositoryRepository.save(repository);

        RepositoryMember member = new RepositoryMember();
        member.setRepository(repository);
        member.setUser(currentUser);
        member.setRole(Role.MASTER);

        repositoryMemberRepository.save(member);
        return RepositoryOutView.fromEntity(repository);
    }

    public List<ItemOutView> fetchLatestRevision(UUID repositoryId) {
        AppUser currentUser = userContextUtil.getCurrentUser();
        Optional<RepositoryMember> currentMember =
                repositoryMemberRepository.findByRepositoryIdAndUserId(repositoryId, currentUser.getId());
        if (currentMember.isEmpty()) {
            throw new AccessDeniedException("You do not have access to this repository.");
        }
        return itemRevisionRepository.findLatestItemsForRepo(repositoryId);
    }

    @Transactional
    public String commitDirectly(UUID repositoryId, List<ItemInView> items, List<MultipartFile> files, String message) {
        AppUser currentUser = userContextUtil.getCurrentUser();
        Optional<RepositoryMember> currentMember =
                repositoryMemberRepository.findByRepositoryIdAndUserId(repositoryId, currentUser.getId());
        if (currentMember.isEmpty() || !currentMember.get().canCommit()) {
            throw new AccessDeniedException("You cannot commit to this repository.");
        }

        return commitService.applyChange(repositoryId, items, files, message, currentUser);
    }
}
