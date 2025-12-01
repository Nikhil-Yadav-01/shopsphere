package com.rudraksha.shopsphere.checkout.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.List;

@Data
public class CheckoutRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<CheckoutItem> items;

    @NotBlank(message = "Shipping address ID is required")
    private String shippingAddressId;

    private String couponCode;

    @Data
    public static class CheckoutItem {
        @NotBlank(message = "Product ID is required")
        private String productId;
        
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        private Integer quantity;
    }
}
