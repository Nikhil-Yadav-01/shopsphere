package com.rudraksha.shopsphere.coupon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rudraksha.shopsphere.coupon.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponResponse {
    private Long id;
    private String code;
    private String description;
    private Coupon.DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscount;
    private BigDecimal minOrderValue;
    private Integer usageLimit;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean isActive;
    private Integer maxUsesPerUser;
    private String applicableCategories;
}
