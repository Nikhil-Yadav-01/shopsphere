package com.rudraksha.shopsphere.returns.repository;

import com.rudraksha.shopsphere.returns.entity.ReturnRequest;
import com.rudraksha.shopsphere.shared.models.enums.ReturnStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReturnRepository extends JpaRepository<ReturnRequest, UUID> {

    Optional<ReturnRequest> findByRmaNumber(String rmaNumber);

    Optional<ReturnRequest> findByOrderId(UUID orderId);

    Page<ReturnRequest> findByUserId(UUID userId, Pageable pageable);

    Page<ReturnRequest> findByStatus(ReturnStatus status, Pageable pageable);

    @Query("SELECT r FROM ReturnRequest r WHERE r.userId = :userId AND r.status = :status")
    Page<ReturnRequest> findByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") ReturnStatus status,
            Pageable pageable
    );

    @Query("SELECT r FROM ReturnRequest r WHERE r.requestedAt >= :startDate AND r.requestedAt <= :endDate")
    List<ReturnRequest> findReturnsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(r) FROM ReturnRequest r WHERE r.status = :status")
    long countByStatus(@Param("status") ReturnStatus status);
}
