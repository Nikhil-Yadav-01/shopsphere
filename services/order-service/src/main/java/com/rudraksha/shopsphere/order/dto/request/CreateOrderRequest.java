package com.rudraksha.shopsphere.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "Shipping address is required")
    @Valid
    private AddressRequest shippingAddress;

    @Valid
    private AddressRequest billingAddress;

    private String currency;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private UUID productId;

        @NotNull(message = "Product name is required")
        private String productName;

        @NotNull(message = "SKU is required")
        private String sku;

        @NotNull(message = "Quantity is required")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        private java.math.BigDecimal unitPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressRequest {
        @NotNull(message = "Full name is required")
        private String fullName;

        private String phone;

        @NotNull(message = "Address line 1 is required")
        private String addressLine1;

        private String addressLine2;

        @NotNull(message = "City is required")
        private String city;

        @NotNull(message = "State is required")
        private String state;

        @NotNull(message = "Postal code is required")
        private String postalCode;

        @NotNull(message = "Country is required")
        private String country;
    }
}
