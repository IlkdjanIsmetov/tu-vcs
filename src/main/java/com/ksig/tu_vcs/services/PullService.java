package com.ksig.tu_vcs.services;

import com.ksig.tu_vcs.repos.ItemRevisionRepository;
import com.ksig.tu_vcs.repos.entities.ItemRevision;
import com.ksig.tu_vcs.services.exceptions.ResourceNotFoundException;
import com.ksig.tu_vcs.services.views.*;
import com.ksig.tu_vcs.repos.entities.enums.SyncStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.ksig.tu_vcs.services.CommitService.ROOT_DOWNLOAD_PATH;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullService {
    private static final String STORAGE_PATH = System.getProperty("user.home") + "/tuVCS_TEST_STORAGE/";
    private final ItemRevisionRepository itemRevisionRepository;
    private final RepositoryService repositoryService;

    public List<SyncItemView> checkSyncStatus(UUID repositoryId, List<LocalItemMetadata> localManifest, String logId) {
        //Метод за вземане на последните версии на файловете
        List<ItemOutView> remoteItems = itemRevisionRepository.findLatestItemsForRepo(repositoryId);

        // Организиране локалните данни в Map за бърз достъп по път на файла
        Map<String, LocalItemMetadata> localMap = localManifest.stream()
                .collect(Collectors.toMap(LocalItemMetadata::getPath, m -> m));

        List<SyncItemView> syncResults = new ArrayList<>();

        for (ItemOutView remote : remoteItems) {
            LocalItemMetadata local = localMap.get(remote.getPath());

            if (local == null) {
                // Случай 1: Файлът е нов на сървъра, няма го локално
                syncResults.add(buildSyncView(remote, SyncStatus.NEW_REMOTE));
                continue;
            }

            // Логика за засичане на промени и конфликти
            SyncStatus status = calculateSyncStatus(remote, local);
            syncResults.add(buildSyncView(remote, status));

            // Премахваме от мапа, за да открием кои файлове са изтрити на сървъра
            localMap.remove(remote.getPath());
        }

        //Всичко останало в локалния мап е изтрито на сървъра (Remote Delete)
        for (LocalItemMetadata localRemaining : localMap.values()) {
            syncResults.add(SyncItemView.builder()
                    .path(localRemaining.getPath())
                    .status(SyncStatus.DELETED_REMOTE)
                    .build());
        }
        log.info("{}: Sync status check completely for repository {}", logId, repositoryId);
        return syncResults;
    }


    public Resource pullFileContent(UUID repositoryId, String storageKey, String logId) {
        //Проверка на достъп и ревизия
        repositoryService.fetchRevision(repositoryId, null);

        try {
            Path filePath = Path.of(STORAGE_PATH).resolve(storageKey);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                log.info("{}: Successfully delivering file {}", logId, filePath.getFileName());
                return resource;
            } else {
                log.error("{}: File not found: {}", logId, filePath.toAbsolutePath());
                throw new RuntimeException("File is not found: " + storageKey);
            }
        } catch (IOException e) {
            log.error("{}: Error reading the file! {}", logId, e.getMessage());
            throw new RuntimeException("Cannot read the file: " + storageKey, e);
        }
    }


    private SyncStatus calculateSyncStatus(ItemOutView remote, LocalItemMetadata local) {
        // 1. Пълно съвпадение
        if (remote.getChecksum().equals(local.getChecksum())) {
            return SyncStatus.UP_TO_DATE;
        }

        // 2. Търсим състоянието на файла в базата данни спрямо последната ревизия на потребителя
        String baseChecksum = itemRevisionRepository
                .findChecksumAtOrBeforeRevision(remote.getId(), local.getLastPulledRevisionNumber())
                .orElse(null);

        // Потребителят е променял локално, ако сегашният му хеш е различен от базовия
        boolean isModifiedLocally = baseChecksum != null && !baseChecksum.equals(local.getChecksum());

        // Сървърът е променен, ако неговият хеш е различен от базовия
        boolean isModifiedRemotely = baseChecksum != null && !baseChecksum.equals(remote.getChecksum());

        if (isModifiedLocally && isModifiedRemotely) {
            return SyncStatus.CONFLICT; // И двамата са правили промени
        } else if (isModifiedRemotely) {
            return SyncStatus.MODIFIED_REMOTE; // Само сървърът е по-нов, безопасен за сваляне
        }

        // Ако само локалният файл е различен от базата, но сървърът е същият като базата
        return SyncStatus.UP_TO_DATE;
    }

    private SyncItemView buildSyncView(ItemOutView remote, SyncStatus status) {
        return SyncItemView.builder()
                .itemId(remote.getId())
                .path(remote.getPath())
                .status(status)
                .serverChecksum(remote.getChecksum())
                .storageKey(remote.getStorageKey())
                .serverRevisionNumber(remote.getRevisionNumber())
                .itemType(remote.getItemType())
                .build();
    }

    public String loadFileContent(String storageKey, String logId) throws IOException {
        Path path = Paths.get(ROOT_DOWNLOAD_PATH + storageKey);
        log.info("{}: Successfully returning content for '{}'", logId, path);
        return Files.readString(path);
    }

    public String getStorageKey(UUID repositoryId, String filePath, Long revision, String logId) {

        if (revision != null) {
            log.info("{}: Fetching storage key for filePath '{}' at revision {}", logId, filePath, revision);
            return itemRevisionRepository.findStorageKeyAtOrBeforeRevision(
                            repositoryId, revision, filePath, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "File '" + filePath + "' did not exist yet at revision " + revision));

        }
        log.info("{}: Fetching latest storage key for filePath '{}'", logId, filePath);
        List<String> results = itemRevisionRepository.findLatestStorageKey(repositoryId, filePath, PageRequest.of(0, 1));
        if (results.isEmpty()) {
            log.warn("{}: No history found for file: '{}'", logId, filePath);
            throw new ResourceNotFoundException("File '" + filePath + "' not found in revision " + revision);
        }
        return results.get(0);
    }
}