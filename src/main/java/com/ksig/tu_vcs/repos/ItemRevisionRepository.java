package com.ksig.tu_vcs.repos;

import com.ksig.tu_vcs.repos.entities.ItemRevision;
import com.ksig.tu_vcs.services.views.ItemOutView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemRevisionRepository extends JpaRepository<ItemRevision, UUID> {
    List<ItemRevision> findByRevisionId(UUID revisionId);

    List<ItemRevision> findByItemId(UUID itemId);

    @Query(value = """
                SELECT 
                    i.id AS id, 
                    i.path AS path, 
                    i.item_type AS itemType, 
                    ir.revision_id AS revisionId, 
                    r.revision_number AS revisionNumber, 
                    ir.checksum AS checksum, 
                    ir.storage_key AS storageKey
                FROM vcs.item i
                JOIN vcs.item_revision ir ON i.id = ir.item_id
                JOIN vcs.revision r ON ir.revision_id = r.id
                WHERE i.repository_id = :repositoryId
                  AND ir.action != 'DELETE'
                  AND r.revision_number = (
                      SELECT MAX(r2.revision_number)
                      FROM vcs.revision r2
                      JOIN vcs.item_revision ir2 ON ir2.revision_id = r2.id
                      WHERE ir2.item_id = ir.item_id
                  )
            """, nativeQuery = true)
    List<ItemOutView> findLatestItemsForRepo(@Param("repositoryId") UUID repositoryId);

    @Query(value = """
                SELECT ir.checksum 
                FROM vcs.item_revision ir 
                JOIN vcs.revision r ON ir.revision_id = r.id 
                WHERE ir.item_id = :itemId 
                  AND r.revision_number <= :revNumber
                ORDER BY r.revision_number DESC 
                LIMIT 1
            """, nativeQuery = true)
    Optional<String> findChecksumAtOrBeforeRevision(@Param("itemId") UUID itemId, @Param("revNumber") Long revNumber);


    @Query(value = """
            SELECT 
                i.id AS id, 
                i.path AS path, 
                i.item_type AS itemType, 
                ir.revision_id AS revisionId, 
                r.revision_number AS revisionNumber, 
                ir.checksum AS checksum, 
                ir.storage_key AS storageKey
            FROM vcs.item i
            JOIN vcs.item_revision ir ON i.id = ir.item_id
            JOIN vcs.revision r ON ir.revision_id = r.id
            WHERE i.repository_id = :repositoryId
              AND r.revision_number <= :revisionNumber
              AND r.revision_number = (
                  SELECT MAX(r2.revision_number)
                  FROM revision r2
                  JOIN item_revision ir2 ON ir2.revision_id = r2.id
                  WHERE ir2.item_id = i.id
                    AND r2.revision_number <= :revisionNumber
              )
              AND ir.action != 'DELETE'
            """, nativeQuery = true)
    List<ItemOutView> findAllFilesAtRevision(
            @Param("repositoryId") UUID repositoryId,
            @Param("revisionNumber") long revisionNumber
    );

    @Query("""
    SELECT ir.storageKey 
    FROM ItemRevision ir 
    WHERE ir.revision.repository.id = :repoId 
      AND ir.item.path = :path 
      AND ir.revision.revisionNumber <= :revNum 
    ORDER BY ir.revision.revisionNumber DESC
""")
    List<String> findStorageKeyAtOrBeforeRevision(
            @Param("repoId") UUID repoId,
            @Param("revNum") Long revNum,
            @Param("path") String path,
            Pageable pageable
    );

    @Query("""
        SELECT ir.storageKey
        FROM ItemRevision ir
        WHERE ir.revision.repository.id = :repoId
          AND ir.item.path = :path
        ORDER BY ir.revision.revisionNumber DESC
""")
    List<String> findLatestStorageKey(
            @Param("repoId") UUID repoId,
            @Param("path") String path,
            Pageable pageable
    );

}
