package com.rudraksha.shopsphere.coupon.repository;

import com.rudraksha.shopsphere.coupon.entity.CouponRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {

    Optional<CouponRedemption> findByOrderId(Long orderId);

    @Query("SELECT cr FROM CouponRedemption cr WHERE cr.couponId = :couponId AND cr.userId = :userId AND cr.status = 'APPLIED'")
    List<CouponRedemption> findUserCouponRedemptions(Long couponId, Long userId);

    @Query("SELECT cr FROM CouponRedemption cr WHERE cr.couponId = :couponId AND cr.status = 'APPLIED'")
    List<CouponRedemption> findCouponRedemptions(Long couponId);
}
