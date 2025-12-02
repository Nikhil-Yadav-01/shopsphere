package com.rudraksha.shopsphere.coupon.mapper;

import com.rudraksha.shopsphere.coupon.dto.CouponResponse;
import com.rudraksha.shopsphere.coupon.entity.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CouponMapper {

    CouponResponse couponToCouponResponse(Coupon coupon);

    Coupon couponResponseToCoupon(CouponResponse couponResponse);
}
