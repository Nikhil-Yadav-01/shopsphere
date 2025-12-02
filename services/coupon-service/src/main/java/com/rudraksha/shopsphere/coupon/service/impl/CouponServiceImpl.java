package com.rudraksha.shopsphere.coupon.service.impl;

import com.rudraksha.shopsphere.coupon.dto.CouponResponse;
import com.rudraksha.shopsphere.coupon.dto.CouponValidationRequest;
import com.rudraksha.shopsphere.coupon.dto.CouponValidationResponse;
import com.rudraksha.shopsphere.coupon.entity.Coupon;
import com.rudraksha.shopsphere.coupon.entity.CouponRedemption;
import com.rudraksha.shopsphere.coupon.exception.CouponExpiredException;
import com.rudraksha.shopsphere.coupon.exception.CouponLimitExceededException;
import com.rudraksha.shopsphere.coupon.exception.CouponNotFoundException;
import com.rudraksha.shopsphere.coupon.mapper.CouponMapper;
import com.rudraksha.shopsphere.coupon.repository.CouponRedemptionRepository;
import com.rudraksha.shopsphere.coupon.repository.CouponRepository;
import com.rudraksha.shopsphere.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository redemptionRepository;
    private final CouponMapper couponMapper;

    @Override
    public CouponResponse createCoupon(CouponResponse dto) {
        Coupon coupon = couponMapper.couponResponseToCoupon(dto);
        Coupon saved = couponRepository.save(coupon);
        log.info("Coupon created: {}", saved.getCode());
        return couponMapper.couponToCouponResponse(saved);
    }

    @Override
    public CouponResponse updateCoupon(Long id, CouponResponse dto) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(dto.getCode(), id));

        coupon.setDescription(dto.getDescription());
        coupon.setDiscountType(dto.getDiscountType());
        coupon.setDiscountValue(dto.getDiscountValue());
        coupon.setMaxDiscount(dto.getMaxDiscount());
        coupon.setMinOrderValue(dto.getMinOrderValue());
        coupon.setValidFrom(dto.getValidFrom());
        coupon.setValidUntil(dto.getValidUntil());
        coupon.setIsActive(dto.getIsActive());
        coupon.setMaxUsesPerUser(dto.getMaxUsesPerUser());
        coupon.setApplicableCategories(dto.getApplicableCategories());

        Coupon updated = couponRepository.save(coupon);
        log.info("Coupon updated: {}", updated.getCode());
        return couponMapper.couponToCouponResponse(updated);
    }

    @Override
    public Optional<CouponResponse> getCoupon(Long id) {
        return couponRepository.findById(id)
                .map(couponMapper::couponToCouponResponse);
    }

    @Override
    public Optional<CouponResponse> getCouponByCode(String code) {
        return couponRepository.findByCode(code)
                .map(couponMapper::couponToCouponResponse);
    }

    @Override
    public Page<CouponResponse> listCoupons(Pageable pageable) {
        Page<Coupon> page = couponRepository.findAll(pageable);
        List<CouponResponse> content = page.getContent().stream()
                .map(couponMapper::couponToCouponResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
        log.info("Coupon deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(CouponValidationRequest request) {
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(request.getCode())
                .orElseThrow(() -> new CouponNotFoundException("Coupon code not found: " + request.getCode()));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidUntil())) {
            throw new CouponExpiredException(coupon.getCode());
        }

        if (request.getOrderTotal().compareTo(coupon.getMinOrderValue() != null ? coupon.getMinOrderValue() : BigDecimal.ZERO) < 0) {
            return CouponValidationResponse.builder()
                    .isValid(false)
                    .message("Order total is below minimum required value")
                    .couponCode(request.getCode())
                    .build();
        }

        // Check usage limits
        if (coupon.getUsageLimit() != null) {
            Integer currentUsage = couponRepository.countRedemptions(coupon.getId());
            if (currentUsage != null && currentUsage >= coupon.getUsageLimit()) {
                throw new CouponLimitExceededException(coupon.getCode());
            }
        }

        // Check per-user limits
        if (coupon.getMaxUsesPerUser() != null && request.getUserId() != null) {
            Integer userUsage = couponRepository.countUserRedemptions(coupon.getId(), request.getUserId());
            if (userUsage != null && userUsage >= coupon.getMaxUsesPerUser()) {
                return CouponValidationResponse.builder()
                        .isValid(false)
                        .message("You have already used this coupon maximum times")
                        .couponCode(request.getCode())
                        .build();
            }
        }

        // Calculate discount
        BigDecimal discountAmount = calculateDiscount(coupon, request.getOrderTotal());
        BigDecimal finalAmount = request.getOrderTotal().subtract(discountAmount);

        return CouponValidationResponse.builder()
                .isValid(true)
                .message("Coupon is valid")
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .couponCode(request.getCode())
                .build();
    }

    @Override
    public void redeemCoupon(String code, Long userId, Long orderId, BigDecimal discountAmount) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found: " + code));

        CouponRedemption redemption = CouponRedemption.builder()
                .couponId(coupon.getId())
                .userId(userId)
                .orderId(orderId)
                .discountAmount(discountAmount)
                .redeemedAt(LocalDateTime.now())
                .status(CouponRedemption.RedemptionStatus.APPLIED)
                .build();

        redemptionRepository.save(redemption);
        log.info("Coupon redeemed: {} by user: {}", code, userId);
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderTotal) {
        BigDecimal discount;

        if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
            discount = orderTotal.multiply(coupon.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);

            if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
                discount = coupon.getMaxDiscount();
            }
        } else {
            discount = coupon.getDiscountValue();
        }

        return discount;
    }
}
