package com.ksig.tu_vcs.services;

import com.ksig.tu_vcs.repos.ItemRepository;
import com.ksig.tu_vcs.repos.ItemRevisionRepository;
import com.ksig.tu_vcs.repos.RepositoryRepository;
import com.ksig.tu_vcs.repos.RevisionRepository;
import com.ksig.tu_vcs.repos.entities.*;
import com.ksig.tu_vcs.repos.entities.enums.Action;
import com.ksig.tu_vcs.repos.entities.enums.ItemType;
import com.ksig.tu_vcs.services.exceptions.CommitException;
import com.ksig.tu_vcs.services.views.ItemInView;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ksig.tu_vcs.services.RepositoryService.ROOT_DOWNLOAD_PATH;

@Service
public class CommitService {
    private final RepositoryRepository repositoryRepository;
    private final ItemRepository itemRepository;
    private final ItemRevisionRepository itemRevisionRepository;
    private final RevisionRepository revisionRepository;

    public CommitService(RepositoryRepository repositoryRepository, ItemRepository itemRepository, ItemRevisionRepository itemRevisionRepository, RevisionRepository revisionRepository) {
        this.repositoryRepository = repositoryRepository;
        this.itemRepository = itemRepository;
        this.itemRevisionRepository = itemRevisionRepository;
        this.revisionRepository = revisionRepository;
    }

    public String applyChange(UUID repositoryId, List<ItemInView> items, List<MultipartFile> files, String message, AppUser currentUser){
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
    public void applyChangeFromCr(ChangeRequestItem crItem, Revision revision,UUID repoId){
        Optional<Item> existingItem = itemRepository.findByRepositoryIdAndPath(repoId, crItem.getPath());
        Item item;
        if(crItem.getAction()==Action.ADD){
            if (existingItem.isPresent()) {
                throw new RuntimeException("Conflict: File already exists at path " + crItem.getPath());
            }
            item = new Item();
            item.setRepository(repositoryRepository.getReferenceById(repoId));
            item.setPath(crItem.getPath());
            item.setItemType(crItem.getItemType());
            item =itemRepository.save(item);
        }
        else {
            item = existingItem.orElseThrow(() ->
                    new RuntimeException("File not found for " + crItem.getAction()));
        }
        ItemRevision itemRevision = new ItemRevision();
        itemRevision.setItem(item);
        itemRevision.setRevision(revision);
        itemRevision.setAction(crItem.getAction());
        itemRevision.setStorageKey(crItem.getStorageKey());
        itemRevision.setChecksum(crItem.getChecksum());
        itemRevision.setFileSize(crItem.getFileSize());

        itemRevisionRepository.save(itemRevision);
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

    public String saveFileToStorage(MultipartFile file) {
        try {
            String uuid = UUID.randomUUID().toString();
            Path downloadFileHere = Path.of(ROOT_DOWNLOAD_PATH).resolve(uuid);
            Files.createDirectories(downloadFileHere.getParent());
            Files.copy(file.getInputStream(), downloadFileHere);
            return uuid;
        } catch (IOException e) {
            System.out.println("!!!!!!!!!!!!!!!!");
            System.out.println(e.getMessage());
            throw new CommitException("Could not download file.");
        }
    }

    public Revision createRevision(UUID repositoryId, String message, AppUser currentUser) {
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

