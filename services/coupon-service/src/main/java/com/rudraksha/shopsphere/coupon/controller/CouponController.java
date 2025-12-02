package com.rudraksha.shopsphere.coupon.controller;

import com.rudraksha.shopsphere.coupon.dto.CouponResponse;
import com.rudraksha.shopsphere.coupon.dto.CouponValidationRequest;
import com.rudraksha.shopsphere.coupon.dto.CouponValidationResponse;
import com.rudraksha.shopsphere.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CouponResponse couponResponse) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.createCoupon(couponResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponResponse> getCoupon(@PathVariable Long id) {
        return couponService.getCoupon(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CouponResponse> getCouponByCode(@PathVariable String code) {
        return couponService.getCouponByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<CouponResponse>> listCoupons(Pageable pageable) {
        return ResponseEntity.ok(couponService.listCoupons(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponResponse couponResponse) {
        return ResponseEntity.ok(couponService.updateCoupon(id, couponResponse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<CouponValidationResponse> validateCoupon(
            @Valid @RequestBody CouponValidationRequest request) {
        return ResponseEntity.ok(couponService.validateCoupon(request));
    }
}
