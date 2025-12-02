package com.rudraksha.shopsphere.media.repository;

import com.rudraksha.shopsphere.media.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    @Query("SELECT m FROM Media m WHERE m.entityType = :entityType AND m.entityId = :entityId ORDER BY m.isPrimary DESC, m.createdAt ASC")
    List<Media> findByEntityTypeAndEntityId(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    @Query("SELECT m FROM Media m WHERE m.entityType = :entityType AND m.entityId = :entityId AND m.isPrimary = true")
    Optional<Media> findPrimaryByEntityTypeAndEntityId(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    @Query("SELECT m FROM Media m WHERE m.id = :id AND m.entityType = :entityType AND m.entityId = :entityId")
    Optional<Media> findByIdAndEntityTypeAndEntityId(@Param("id") Long id, @Param("entityType") String entityType, @Param("entityId") Long entityId);

    void deleteByEntityTypeAndEntityId(String entityType, Long entityId);
}
