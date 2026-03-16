package com.ksig.tu_vcs.services;


import com.ksig.tu_vcs.repos.ItemRepository;
import com.ksig.tu_vcs.repos.ItemRevisionRepository;
import com.ksig.tu_vcs.repos.RepositoryMemberRepository;
import com.ksig.tu_vcs.repos.RepositoryRepository;
import com.ksig.tu_vcs.repos.RevisionRepository;
import com.ksig.tu_vcs.repos.entities.Repository;
import com.ksig.tu_vcs.repos.entities.RepositoryMember;
import com.ksig.tu_vcs.repos.entities.Revision;
import com.ksig.tu_vcs.repos.entities.enums.Role;
import com.ksig.tu_vcs.services.views.RepositoryView;
import com.ksig.tu_vcs.utils.UserContextUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class RepositoryService {
    private final RepositoryRepository repositoryRepository;
    private final RepositoryMemberRepository repositoryMemberRepository;
    private final UserContextUtil userContextUtil;
    private final RevisionRepository revisionRepository;
    private final ItemRepository itemRepository;
    private final ItemRevisionRepository itemRevisionRepository;

    public RepositoryService(RepositoryRepository repositoryRepository, RepositoryMemberRepository repositoryMemberRepository,
                             UserContextUtil userContextUtil, RevisionRepository revisionRepository, ItemRepository itemRepository,
                             ItemRevisionRepository itemRevisionRepository) {
        this.repositoryRepository = repositoryRepository;
        this.repositoryMemberRepository = repositoryMemberRepository;
        this.userContextUtil = userContextUtil;
        this.revisionRepository = revisionRepository;
        this.itemRepository = itemRepository;
        this.itemRevisionRepository = itemRevisionRepository;
    }

    @Transactional
    public Repository createRepository(RepositoryView view) {
        Repository repository = new Repository();
        repository.setName(view.getRepositoryName());
        repository.setDescription(view.getDescription());
        repository.setOwner(userContextUtil.getCurrentUser());
        repository.setRequiresApprovalByDefault(true);

        repositoryRepository.save(repository);

        RepositoryMember member = new RepositoryMember();
        member.setRepository(repository);
        member.setUser(userContextUtil.getCurrentUser());
        member.setRole(Role.MASTER);

        repositoryMemberRepository.save(member);

        return repository;
    }

    @Transactional
    public String commitDirectly(UUID repositoryId, List<String> path, List<MultipartFile> files, String message) {
        try {

        } catch (Exception e) {
        }
        return "OK";
    }

    private Revision createRevision(UUID repositoryId, String message) {
        Revision revision = new Revision();
        revision.setRepository(repositoryRepository.getReferenceById(repositoryId));
        revision.setAuthor(userContextUtil.getCurrentUser());
        revision.setMessage(message);
        Optional<Revision> previousRevision = revisionRepository.findLatestRevision(repositoryId);
        Long revisionNumber;
        revisionNumber = previousRevision.map(value -> value.getRevisionNumber() + 1).orElse(1L);
        revision.setRevisionNumber(revisionNumber);
        return revisionRepository.save(revision);
    }
}
