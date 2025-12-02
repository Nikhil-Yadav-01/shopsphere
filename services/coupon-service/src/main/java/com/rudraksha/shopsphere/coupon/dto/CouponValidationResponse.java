package com.rudraksha.shopsphere.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponValidationResponse {
    private Boolean isValid;
    private String message;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String couponCode;
}
