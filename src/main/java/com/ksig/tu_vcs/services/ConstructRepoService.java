package com.ksig.tu_vcs.services;

import com.ksig.tu_vcs.repos.ItemRevisionRepository;
import com.ksig.tu_vcs.repos.RepositoryRepository;
import com.ksig.tu_vcs.repos.RevisionRepository;
import com.ksig.tu_vcs.repos.entities.Repository;
import com.ksig.tu_vcs.repos.entities.Revision;
import com.ksig.tu_vcs.repos.entities.enums.ItemType;
import com.ksig.tu_vcs.services.views.ItemOutView;
import com.ksig.tu_vcs.services.views.RepositoryOutView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class ConstructRepoService {
    private static final String TEMP_ZIP_DIR = System.getProperty("user.home") + "/tuVCS_TEMP_ZIP";
    private final ObjectMapper objectMapper;
    private final RepositoryRepository repositoryRepository;
    private final ItemRevisionRepository itemRevisionRepository;
    private final RevisionRepository revisionRepository;

    public ConstructRepoService(@Qualifier("autoCloseFalseMapper") ObjectMapper objectMapper, RepositoryRepository repositoryRepository, ItemRevisionRepository itemRevisionRepository,
                                RevisionRepository revisionRepository) {
        this.objectMapper = objectMapper;
        this.repositoryRepository = repositoryRepository;
        this.itemRevisionRepository = itemRevisionRepository;
        this.revisionRepository = revisionRepository;
    }

    public Path constructZipFolder(UUID repositoryId, String logId) {
        List<ItemOutView> items = itemRevisionRepository.findLatestItemsForRepo(repositoryId);
        //сортираме item-ите, така че първо да са директориите а после файловете
        items = items.stream().sorted(new Comparator<ItemOutView>() {
            @Override
            public int compare(ItemOutView o1, ItemOutView o2) {
                if (o1.getItemType().equals(o2.getItemType())) {
                    return 0;
                } else if (o1.getItemType().equals(ItemType.DIRECTORY) && o2.getItemType().equals(ItemType.FILE)) {
                    return -1;
                } else if (o1.getItemType().equals(ItemType.FILE) && o2.getItemType().equals(ItemType.DIRECTORY)) {
                    return 1;
                }
                return 0;
            }
        }).toList();
        Repository repo = repositoryRepository.findById(repositoryId).orElseThrow();
        Revision revision = revisionRepository.findLatestRevision(repositoryId).orElseThrow();
        RepositoryOutView repoOut = RepositoryOutView.fromEntity(repo);
        repoOut.setRevision(revision.getRevisionNumber());
        String repoUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/repositories/" + repoOut.getId())
                .toUriString();
        repoOut.setUrl(repoUrl);
        Path repoRoot = Path.of(TEMP_ZIP_DIR).resolve(repo.getName());
        Path storageDIr = Path.of(CommitService.ROOT_DOWNLOAD_PATH);
        try {
            Files.createDirectories(repoRoot);
            for (ItemOutView item : items) {
                if (item.getItemType().equals(ItemType.DIRECTORY)) {
                    Files.createDirectories(repoRoot.resolve(item.getPath()));
                }
                if (item.getItemType().equals(ItemType.FILE)) {
                    Path storedFile = storageDIr.resolve(item.getStorageKey());
                    Path repoFile = repoRoot.resolve(item.getPath());
                    Files.copy(storedFile, repoFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            Path zipPath = Path.of(TEMP_ZIP_DIR).resolve(repo.getName() + ".zip");
            zipDirectory(repoRoot, zipPath, items, repoOut);
            deleteDirectory(repoRoot, logId);
            return zipPath;
        } catch (IOException e) {
            log.error("{}: Error creating zipped repo.", logId, e);
            throw new RuntimeException("Error while cloning repo!");
        }
    }

    private void deleteDirectory(Path dir, String logId) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.error("{}: Failed to delete file", logId, e);
                        }
                    });
        }
    }

    private void zipDirectory(Path sourceDirPath, Path zipFilePath, List<ItemOutView> items, RepositoryOutView repo)
            throws IOException {

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {

            Files.walk(sourceDirPath)
                    .forEach(path -> {
                        if (sourceDirPath.equals(path)) {
                            return;
                        }

                        try {
                            String relativePath = sourceDirPath.relativize(path).toString();
                            relativePath = relativePath.replace("\\", "/");
                            if (Files.isDirectory(path)) {
                                relativePath += "/";
                            }

                            ZipEntry zipEntry = new ZipEntry(relativePath);
                            zos.putNextEntry(zipEntry);

                            if (!Files.isDirectory(path)) {
                                try (var in = Files.newInputStream(path)) {
                                    in.transferTo(zos);
                                }
                            }

                            zos.closeEntry();
                        } catch (IOException e) {
                            System.err.println("Failed to process path: " + path);
                            e.printStackTrace();
                        }
                    });

            String repoMeta = "/.tu_vcs_repo/repo.json";
            ZipEntry zipEntry = new ZipEntry(repoMeta);
            zos.putNextEntry(zipEntry);
            zos.write(objectMapper.writeValueAsBytes(repo));
            zos.closeEntry();
            String itemMeta = "/.tu_vcs_repo/items.json";
            zipEntry = new ZipEntry(itemMeta);
            zos.putNextEntry(zipEntry);
            zos.write(objectMapper.writeValueAsBytes(items));
            zos.closeEntry();
        }
    }
}
