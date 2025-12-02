package com.rudraksha.shopsphere.coupon.service;

import com.rudraksha.shopsphere.coupon.dto.CouponResponse;
import com.rudraksha.shopsphere.coupon.dto.CouponValidationRequest;
import com.rudraksha.shopsphere.coupon.dto.CouponValidationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CouponService {

    CouponResponse createCoupon(CouponResponse couponResponse);

    CouponResponse updateCoupon(Long id, CouponResponse couponResponse);

    Optional<CouponResponse> getCoupon(Long id);

    Optional<CouponResponse> getCouponByCode(String code);

    Page<CouponResponse> listCoupons(Pageable pageable);

    void deleteCoupon(Long id);

    CouponValidationResponse validateCoupon(CouponValidationRequest request);

    void redeemCoupon(String code, Long userId, Long orderId, java.math.BigDecimal discountAmount);
}
