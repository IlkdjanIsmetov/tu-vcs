package com.ksig.tu_vcs.services;


import com.ksig.tu_vcs.repos.*;
import com.ksig.tu_vcs.repos.entities.*;
import com.ksig.tu_vcs.repos.entities.enums.Role;
import com.ksig.tu_vcs.services.exceptions.ResourceAlreadyExistsException;
import com.ksig.tu_vcs.services.exceptions.ResourceNotFoundException;
import com.ksig.tu_vcs.services.views.ItemInView;
import com.ksig.tu_vcs.services.views.ItemOutView;
import com.ksig.tu_vcs.services.views.RepositoryInView;
import com.ksig.tu_vcs.services.views.RepositoryOutView;
import com.ksig.tu_vcs.utils.UserContextUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class RepositoryService {


    private final RepositoryRepository repositoryRepository;
    private final RepositoryMemberRepository repositoryMemberRepository;
    private final UserContextUtil userContextUtil;
    private final ItemRevisionRepository itemRevisionRepository;
    private final CommitService commitService;
    private final AppUserRepository appUserRepository;
    private final ConstructRepoService constructRepoService;


    public RepositoryService(RepositoryRepository repositoryRepository, RepositoryMemberRepository repositoryMemberRepository,
                             UserContextUtil userContextUtil,
                             ItemRevisionRepository itemRevisionRepository, CommitService commitService,
                             AppUserRepository appUserRepository, ConstructRepoService constructRepoService) {
        this.repositoryRepository = repositoryRepository;
        this.repositoryMemberRepository = repositoryMemberRepository;
        this.userContextUtil = userContextUtil;
        this.itemRevisionRepository = itemRevisionRepository;
        this.commitService = commitService;
        this.appUserRepository = appUserRepository;
        this.constructRepoService = constructRepoService;
    }

    @Transactional
    public RepositoryOutView createRepository(RepositoryInView view, String logId) {
        if (repositoryRepository.findByName(view.getRepositoryName()).isPresent()) {
            log.error("{}: Repository with name \"{}\" already exists", logId, view.getRepositoryName());
            throw new ResourceAlreadyExistsException("Repository with name " + view.getRepositoryName() + " already exists");
        }
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
        log.info("{}: Created repository \"{}\" for user \"{}\"", logId, repository.getName(), currentUser.getUsername());
        return RepositoryOutView.fromEntity(repository);
    }

    @Transactional
    public void deleteRepository(UUID repositoryId, String logId) {
        AppUser currentUser = userContextUtil.getCurrentUser();
        Optional<RepositoryMember> currentMember =
                repositoryMemberRepository.findByRepositoryIdAndUserId(repositoryId, currentUser.getId());
        if (currentMember.isEmpty() || !currentMember.get().getRole().equals(Role.MASTER)) {
            throw new AccessDeniedException("You cannot delete this repository.");
        }
        repositoryMemberRepository.delete(currentMember.get());
        log.info("{}: Deleted repository with id \"{}\"", logId, repositoryId);
    }

    public List<ItemOutView> fetchRevision(UUID repositoryId, Long revisionNumber) {
        AppUser currentUser = userContextUtil.getCurrentUser();
        Optional<RepositoryMember> currentMember =
                repositoryMemberRepository.findByRepositoryIdAndUserId(repositoryId, currentUser.getId());
        if (currentMember.isEmpty()) {
            throw new AccessDeniedException("You do not have access to this repository.");
        }
        if (revisionNumber == null) {
            return itemRevisionRepository.findLatestItemsForRepo(repositoryId);
        }
        return itemRevisionRepository.findAllFilesAtRevision(repositoryId, revisionNumber);
    }

    @Transactional
    public String commitDirectly(UUID repositoryId, List<ItemInView> items, List<MultipartFile> files, String message, String logId) {
        AppUser currentUser = userContextUtil.getCurrentUser();
        Optional<RepositoryMember> currentMember =
                repositoryMemberRepository.findByRepositoryIdAndUserId(repositoryId, currentUser.getId());
        if (currentMember.isEmpty() || !currentMember.get().canCommit()) {
            throw new AccessDeniedException("You cannot commit to this repository.");
        }

        return commitService.applyChange(repositoryId, items, files, message, currentUser, logId);
    }

    @Transactional
    public void addMember(UUID repositoryId, String username, Role role, String logId) {
        AppUser currentUser = userContextUtil.getCurrentUser();
        Optional<RepositoryMember> currentMember =
                repositoryMemberRepository.findByRepositoryIdAndUserId(repositoryId, currentUser.getId());
        AppUser userToAdd = appUserRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Username " + username + " not found"));
        if (currentMember.isEmpty() || !currentMember.get().getRole().equals(Role.MASTER)) {
            throw new AccessDeniedException("You cannot add members to this repository.");
        }
        RepositoryMember memberToAdd = new RepositoryMember();
        memberToAdd.setRepository(repositoryRepository.getReferenceById(repositoryId));
        memberToAdd.setUser(userToAdd);
        memberToAdd.setRole(role);
        repositoryMemberRepository.save(memberToAdd);
        log.info("{}: User \"{}\" added member \"{}\" to repository {}", logId, currentUser.getUsername(), username, repositoryId);
    }

    @Transactional
    public void kickMember(UUID repositoryId, String username, String logId) {
        AppUser currentUser = userContextUtil.getCurrentUser();
        Optional<RepositoryMember> currentMember =
                repositoryMemberRepository.findByRepositoryIdAndUserId(repositoryId, currentUser.getId());
        if (currentMember.isEmpty() || !currentMember.get().getRole().equals(Role.MASTER)) {
            throw new AccessDeniedException("You cannot kick members from this repository.");
        }
        AppUser userToKick = appUserRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Username " + username + " not found"));
        Optional<RepositoryMember> memberToKick =
                repositoryMemberRepository.findByRepositoryIdAndUserId(repositoryId, userToKick.getId());
        repositoryMemberRepository.delete(memberToKick.get());
        log.info("{}: User \"{}\" kicked member \"{}\" from repository {}", logId, currentUser.getUsername(), username, repositoryId);
    }

    public Path getZippedRepo(UUID repositoryId, String logId) {
        AppUser currentUser = userContextUtil.getCurrentUser();
        Optional<RepositoryMember> currentMember =
                repositoryMemberRepository.findByRepositoryIdAndUserId(repositoryId, currentUser.getId());
        if (currentMember.isEmpty()) {
            throw new AccessDeniedException("You cannot clone this repository.");
        }
        return constructRepoService.constructZipFolder(repositoryId, logId);
    }

    private String generateRepositoryUrl(UUID repositoryId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/repositories/")
                .path(repositoryId.toString())
                .toUriString();
    }

    private RepositoryOutView convertToView(Repository repository) {
        RepositoryOutView view = RepositoryOutView.fromEntity(repository);
        view.setUrl(generateRepositoryUrl(repository.getId()));
        return view;
    }

    public List<RepositoryOutView> findAllRepositories() {
        return repositoryRepository.findAll().stream()
                .map(this::convertToView)
                .collect(Collectors.toList());

    }

    public List<RepositoryOutView> findUserRepositories(UUID userId) {
        return repositoryRepository.findByOwnerId(userId).stream()
                .map(this::convertToView)
                .collect(Collectors.toList());
    }

    public List<RepositoryOutView> searchRepositories(String search) {
        return repositoryRepository.findByNameContainingIgnoreCase(search).stream()
                .map(this::convertToView)
                .collect(Collectors.toList());
    }
}
