package com.ksig.tu_vcs.services;

import com.ksig.tu_vcs.repos.ItemRevisionRepository;
import com.ksig.tu_vcs.services.views.*;
import com.ksig.tu_vcs.services.views.SyncStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PullService {
    private static final String STORAGE_PATH = System.getProperty("user.home") + "/tuVCS_TEST_STORAGE/";
    private final ItemRevisionRepository itemRevisionRepository;
    private final RepositoryService repositoryService;

    /**
     * СТЪПКА 1: Проверка за промени
     * Сравнява какво има на сървъра с локалния манифест на потребителя.
     */
    public List<SyncItemView> checkSyncStatus(UUID repositoryId, List<LocalItemMetadata> localManifest) {
        // Използваме твоя съществуващ метод за вземане на последните версии на файловете
        List<ItemOutView> remoteItems = itemRevisionRepository.findLatestItemsForRepo(repositoryId);

        // Организираме локалните данни в Map за бърз достъп по път на файла
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

        // Случай 2: Всичко останало в локалния мап е изтрито на сървъра (Remote Delete)
        for (LocalItemMetadata localRemaining : localMap.values()) {
            syncResults.add(SyncItemView.builder()
                    .path(localRemaining.getPath())
                    .status(SyncStatus.DELETED_REMOTE)
                    .build());
        }

        return syncResults;
    }

    /**
     * СТЪПКА 2: Изтегляне на конкретно съдържание
     * Позволява на потребителя да изтегли избрания от него файл.
     */
    public Resource pullFileContent(UUID repositoryId, String storageKey) {
        // Твоята логика за проверка на достъп и ревизия
        repositoryService.fetchLatestRevision(repositoryId);

        try {
            Path filePath = Path.of(STORAGE_PATH).resolve(storageKey);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Файлът не беше намерен в хранилището: " + storageKey);
            }
        } catch (IOException e) {
            throw new RuntimeException("Грешка при четене на файл: " + storageKey, e);
        }
    }

    /**
     * Вътрешна логика за "Three-way merge" сравнение
     */
    private SyncStatus calculateSyncStatus(ItemOutView remote, LocalItemMetadata local) {
        // 1. Пълно съвпадение
        if (remote.getChecksum().equals(local.getChecksum())) {
            return SyncStatus.UP_TO_DATE;
        }

        // 2. Търсим състоянието на файла в базата данни спрямо последната ревизия на потребителя
        // Използваме новия метод, който добавихме в ItemRevisionRepository
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
                .build();
    }
}