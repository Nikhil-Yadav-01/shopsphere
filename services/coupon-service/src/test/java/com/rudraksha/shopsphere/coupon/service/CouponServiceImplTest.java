package com.rudraksha.shopsphere.coupon.service;

import com.rudraksha.shopsphere.coupon.dto.CouponResponse;
import com.rudraksha.shopsphere.coupon.dto.CouponValidationRequest;
import com.rudraksha.shopsphere.coupon.dto.CouponValidationResponse;
import com.rudraksha.shopsphere.coupon.entity.Coupon;
import com.rudraksha.shopsphere.coupon.exception.CouponExpiredException;
import com.rudraksha.shopsphere.coupon.exception.CouponNotFoundException;
import com.rudraksha.shopsphere.coupon.mapper.CouponMapper;
import com.rudraksha.shopsphere.coupon.repository.CouponRedemptionRepository;
import com.rudraksha.shopsphere.coupon.repository.CouponRepository;
import com.rudraksha.shopsphere.coupon.service.impl.CouponServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponRedemptionRepository redemptionRepository;

    @Mock
    private CouponMapper couponMapper;

    @InjectMocks
    private CouponServiceImpl couponService;

    private Coupon testCoupon;
    private CouponResponse couponResponse;

    @BeforeEach
    void setUp() {
        testCoupon = Coupon.builder()
                .code("SAVE20")
                .description("20% off")
                .discountType(Coupon.DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("20"))
                .maxDiscount(new BigDecimal("100"))
                .minOrderValue(new BigDecimal("50"))
                .usageLimit(100)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .build();
        testCoupon.setId(1L);

        couponResponse = CouponResponse.builder()
                .id(1L)
                .code("SAVE20")
                .discountType(Coupon.DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("20"))
                .build();
    }

    @Test
    void testValidateCoupon_Success() {
        CouponValidationRequest request = CouponValidationRequest.builder()
                .code("SAVE20")
                .orderTotal(new BigDecimal("100"))
                .userId(1L)
                .build();

        when(couponRepository.findByCodeAndIsActiveTrue("SAVE20"))
                .thenReturn(Optional.of(testCoupon));
        when(couponRepository.countRedemptions(1L))
                .thenReturn(10);

        CouponValidationResponse response = couponService.validateCoupon(request);

        assertTrue(response.getIsValid());
        assertEquals(new BigDecimal("20"), response.getDiscountAmount());
    }

    @Test
    void testValidateCoupon_CouponNotFound() {
        CouponValidationRequest request = CouponValidationRequest.builder()
                .code("INVALID")
                .orderTotal(new BigDecimal("100"))
                .build();

        when(couponRepository.findByCodeAndIsActiveTrue("INVALID"))
                .thenReturn(Optional.empty());

        assertThrows(CouponNotFoundException.class, () -> couponService.validateCoupon(request));
    }

    @Test
    void testValidateCoupon_BelowMinimumOrder() {
        CouponValidationRequest request = CouponValidationRequest.builder()
                .code("SAVE20")
                .orderTotal(new BigDecimal("30"))
                .build();

        when(couponRepository.findByCodeAndIsActiveTrue("SAVE20"))
                .thenReturn(Optional.of(testCoupon));

        CouponValidationResponse response = couponService.validateCoupon(request);

        assertFalse(response.getIsValid());
    }

    @Test
    void testValidateCoupon_Expired() {
        Coupon expiredCoupon = Coupon.builder()
                .code("EXPIRED")
                .validFrom(LocalDateTime.now().minusDays(10))
                .validUntil(LocalDateTime.now().minusDays(1))
                .isActive(true)
                .build();
        expiredCoupon.setId(1L);

        CouponValidationRequest request = CouponValidationRequest.builder()
                .code("EXPIRED")
                .orderTotal(new BigDecimal("100"))
                .build();

        when(couponRepository.findByCodeAndIsActiveTrue("EXPIRED"))
                .thenReturn(Optional.of(expiredCoupon));

        assertThrows(CouponExpiredException.class, () -> couponService.validateCoupon(request));
    }

    @Test
    void testCreateCoupon() {
        when(couponMapper.couponResponseToCoupon(any()))
                .thenReturn(testCoupon);
        when(couponRepository.save(any(Coupon.class)))
                .thenReturn(testCoupon);
        when(couponMapper.couponToCouponResponse(any()))
                .thenReturn(couponResponse);

        CouponResponse result = couponService.createCoupon(couponResponse);

        assertNotNull(result);
        assertEquals("SAVE20", result.getCode());
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    void testRedeemCoupon() {
        when(couponRepository.findByCode("SAVE20"))
                .thenReturn(Optional.of(testCoupon));

        couponService.redeemCoupon("SAVE20", 1L, 100L, new BigDecimal("20"));

        verify(redemptionRepository, times(1)).save(any());
    }
}
