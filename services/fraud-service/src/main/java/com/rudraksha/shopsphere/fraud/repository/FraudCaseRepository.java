package com.rudraksha.shopsphere.fraud.repository;

import com.rudraksha.shopsphere.fraud.entity.FraudCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudCaseRepository extends JpaRepository<FraudCase, Long> {

    Page<FraudCase> findByStatusAndDeletedFalse(FraudCase.CaseStatus status, Pageable pageable);

    Page<FraudCase> findByRiskLevelAndDeletedFalse(FraudCase.RiskLevel level, Pageable pageable);

    Page<FraudCase> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    @Query("SELECT f FROM FraudCase f WHERE f.fraudScore >= :minScore AND f.deleted = false ORDER BY f.createdAt DESC")
    List<FraudCase> findHighRiskCases(Integer minScore);

    List<FraudCase> findByOrderIdAndDeletedFalse(Long orderId);
}
