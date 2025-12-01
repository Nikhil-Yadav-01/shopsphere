package com.rudraksha.shopsphere.inventory.repository;

import com.rudraksha.shopsphere.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    Page<StockMovement> findByInventoryId(UUID inventoryId, Pageable pageable);

    List<StockMovement> findByInventoryIdAndType(UUID inventoryId, StockMovement.MovementType type);

    List<StockMovement> findByReferenceId(UUID referenceId);

    List<StockMovement> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<StockMovement> findByInventoryIdAndCreatedAtBetween(UUID inventoryId, LocalDateTime start, LocalDateTime end);
}
