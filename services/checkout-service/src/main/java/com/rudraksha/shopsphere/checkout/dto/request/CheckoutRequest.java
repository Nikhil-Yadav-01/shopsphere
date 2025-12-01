package com.rudraksha.shopsphere.checkout.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class CheckoutRequest {
    @NotBlank
    private String userId;

    @NotEmpty
    private List<CheckoutItem> items;

    @NotBlank
    private String shippingAddressId;

    private String couponCode;

    @Data
    public static class CheckoutItem {
        @NotBlank
        private String productId;
        private Integer quantity;
    }
}
