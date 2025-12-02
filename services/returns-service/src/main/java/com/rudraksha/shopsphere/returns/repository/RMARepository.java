package com.rudraksha.shopsphere.returns.repository;

import com.rudraksha.shopsphere.returns.entity.RMA;
import com.rudraksha.shopsphere.shared.models.enums.ReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RMARepository extends JpaRepository<RMA, UUID> {

    Optional<RMA> findByRmaNumber(String rmaNumber);

    Optional<RMA> findByReturnRequestId(UUID returnRequestId);

    @Query("SELECT r FROM RMA r WHERE r.status = :status")
    List<RMA> findByStatus(@Param("status") ReturnStatus status);

    @Query("SELECT r FROM RMA r WHERE r.isExpired = false AND r.expiryDate < :now")
    List<RMA> findExpiredRMAs(@Param("now") LocalDateTime now);
}
