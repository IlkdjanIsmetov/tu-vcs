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
import com.ksig.tu_vcs.services.views.RepositoryView;
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
    //TODO move somewhere else later
    private static final String ROOT_DOWNLOAD_PATH = "/home/djani/testdir";

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
        AppUser currentUser = userContextUtil.getCurrentUser();
        Repository repository = new Repository();
        repository.setName(view.getRepositoryName());
        repository.setDescription(view.getDescription());
        repository.setOwner(currentUser);
        repository.setRequiresApprovalByDefault(true);

        repositoryRepository.save(repository);

        RepositoryMember member = new RepositoryMember();
        member.setRepository(repository);
        member.setUser(currentUser);
        member.setRole(Role.MASTER);

        repositoryMemberRepository.save(member);

        return repository;
    }

    public List<ItemOutView> fetchLatestRevision(UUID repositoryId) {
        AppUser currentUser = userContextUtil.getCurrentUser();
        Optional<RepositoryMember> currentMember = repositoryMemberRepository.findByRepositoryIdAndUserId(repositoryId, currentUser.getId());
        if (currentMember.isEmpty()) {
            throw new AccessDeniedException("You do not have access to this repository.");
        }
        return itemRevisionRepository.findLatestItemsForRepo(repositoryId);
    }

    @Transactional
    public String commitDirectly(UUID repositoryId, List<ItemInView> items, List<MultipartFile> files, String message) {
        AppUser currentUser = userContextUtil.getCurrentUser();
        Optional<RepositoryMember> currentMember = repositoryMemberRepository.findByRepositoryIdAndUserId(repositoryId, currentUser.getId());
        if (currentMember.isEmpty() || !currentMember.get().canCommit()) {
            throw new AccessDeniedException("You cannot commit to this repository.");
        }

        //първо правим нов ревижън
        Revision revision = createRevision(repositoryId, message, currentUser);
        //правя листа към мап с ключ името на файла, като разчитам че клиента ще опише връзките в ItemView
        Map<String, MultipartFile> fileMap = files.stream()
                .collect(Collectors.toMap(MultipartFile::getOriginalFilename, file -> file));

        for (ItemInView item : items) {
            if (item.getItemType().equals(ItemType.FILE)) {
                switch (item.getAction()) {
                    case ADD: addFile(repositoryId, item, fileMap.get(item.getFileRef()), revision); break;
                    case MODIFY: modifyFile(item, fileMap.get(item.getFileRef()), revision); break;
                    case DELETE: deleteFile(item, revision); break;
                }
            }
            if (item.getItemType().equals(ItemType.DIRECTORY)) {
                switch (item.getAction()) {
                    case ADD: addDir(repositoryId, item, revision); break;
//                    case MODIFY: break; май няма как да е модифайд
                    case DELETE: deleteDir(item, revision); break;
                }
            }
        }
        return "OK";
    }

    private void addFile(UUID repositoryId , ItemInView itemInView, MultipartFile file, Revision revision) {
        Item item = new Item();
        item.setRepository(repositoryRepository.getReferenceById(repositoryId));
        item.setItemType(ItemType.FILE);
        item.setPath(itemInView.getPath());
        item = itemRepository.save(item);
        String storageKey = saveFileToStorage(file);
        ItemRevision itemRevision = new ItemRevision();
        itemRevision.setItem(item);
        itemRevision.setAction(Action.ADD);
        itemRevision.setRevision(revision);
        itemRevision.setChecksum(itemInView.getChecksum());
        itemRevision.setFileSize(file.getSize());
        itemRevision.setStorageKey(storageKey);
        itemRevisionRepository.save(itemRevision);
    }

    private void modifyFile(ItemInView itemInView, MultipartFile file, Revision revision) {
        String storageKey = saveFileToStorage(file);
        ItemRevision itemRevision = new ItemRevision();
        itemRevision.setItem(itemRepository.getReferenceById(itemInView.getItemId()));
        itemRevision.setAction(Action.MODIFY);
        itemRevision.setRevision(revision);
        itemRevision.setChecksum(itemInView.getChecksum());
        itemRevision.setFileSize(file.getSize());
        itemRevision.setStorageKey(storageKey);
        itemRevisionRepository.save(itemRevision);
    }

    private void deleteFile(ItemInView itemInView, Revision revision) {
        ItemRevision itemRevision = new ItemRevision();
        itemRevision.setItem(itemRepository.getReferenceById(itemInView.getItemId()));
        itemRevision.setAction(Action.DELETE);
        itemRevision.setRevision(revision);
        itemRevisionRepository.save(itemRevision);
    }

    private void addDir(UUID repositoryId, ItemInView itemInView, Revision revision) {
        Item item = new Item();
        item.setRepository(repositoryRepository.getReferenceById(repositoryId));
        item.setItemType(ItemType.DIRECTORY);
        item.setPath(itemInView.getPath());
        item = itemRepository.save(item);
        ItemRevision itemRevision = new ItemRevision();
        itemRevision.setItem(item);
        itemRevision.setAction(Action.ADD);
        itemRevision.setRevision(revision);
        itemRevisionRepository.save(itemRevision);
    }

    private void deleteDir(ItemInView itemInView, Revision revision) {
        ItemRevision itemRevision = new ItemRevision();
        itemRevision.setItem(itemRepository.getReferenceById(itemInView.getItemId()));
        itemRevision.setAction(Action.DELETE);
        itemRevision.setRevision(revision);
        itemRevisionRepository.save(itemRevision);
    }

    private String saveFileToStorage(MultipartFile file) {
       try {
           String uuid = UUID.randomUUID().toString();
           Files.copy(file.getInputStream(), Path.of(ROOT_DOWNLOAD_PATH).resolve(uuid));
           return uuid;
       } catch (IOException e) {
           throw new CommitException("Could not download file.");
       }
    }

    private Revision createRevision(UUID repositoryId, String message, AppUser currentUser) {
        Revision revision = new Revision();
        revision.setRepository(repositoryRepository.getReferenceById(repositoryId));
        revision.setAuthor(currentUser);
        revision.setMessage(message);
        Optional<Revision> previousRevision = revisionRepository.findLatestRevision(repositoryId);
        Long revisionNumber;
        revisionNumber = previousRevision.map(value -> value.getRevisionNumber() + 1).orElse(1L);
        revision.setRevisionNumber(revisionNumber);
        return revisionRepository.save(revision);
    }
}
