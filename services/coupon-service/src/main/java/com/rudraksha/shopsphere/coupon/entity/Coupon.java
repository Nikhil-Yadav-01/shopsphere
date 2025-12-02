package com.rudraksha.shopsphere.coupon.entity;

import com.rudraksha.shopsphere.shared.db.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends BaseEntity {

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType; // PERCENTAGE or FIXED_AMOUNT

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "max_discount")
    private BigDecimal maxDiscount; // For percentage discounts

    @Column(name = "min_order_value")
    private BigDecimal minOrderValue;

    @Column(name = "usage_limit")
    private Integer usageLimit; // Null = unlimited

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "max_uses_per_user")
    private Integer maxUsesPerUser;

    @Column(name = "applicable_categories")
    private String applicableCategories; // Comma-separated or null for all

    public enum DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT
    }
}
