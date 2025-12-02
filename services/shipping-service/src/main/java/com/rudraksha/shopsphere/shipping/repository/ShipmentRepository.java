package com.rudraksha.shopsphere.shipping.repository;

import com.rudraksha.shopsphere.shipping.entity.Shipment;
import com.rudraksha.shopsphere.shared.models.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    Optional<Shipment> findByOrderId(UUID orderId);

    Page<Shipment> findByStatus(ShipmentStatus status, Pageable pageable);

    @Query("SELECT s FROM Shipment s LEFT JOIN FETCH s.trackingEvents WHERE s.id = :id")
    Optional<Shipment> findByIdWithEvents(@Param("id") UUID id);

    boolean existsByTrackingNumber(String trackingNumber);
}
