package com.rudraksha.shopsphere.coupon.repository;

import com.rudraksha.shopsphere.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeAndIsActiveTrue(String code);

    Optional<Coupon> findByCode(String code);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.validFrom <= :now AND c.validUntil >= :now")
    List<Coupon> findActiveCoupons(LocalDateTime now);

    @Query("SELECT COUNT(r) FROM CouponRedemption r WHERE r.couponId = :couponId AND r.status = 'APPLIED'")
    Integer countRedemptions(Long couponId);

    @Query("SELECT COUNT(r) FROM CouponRedemption r WHERE r.couponId = :couponId AND r.userId = :userId AND r.status = 'APPLIED'")
    Integer countUserRedemptions(Long couponId, Long userId);
}
