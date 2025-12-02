package com.rudraksha.shopsphere.shipping.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShipmentRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotBlank(message = "Carrier is required")
    private String carrier;

    @NotNull(message = "Shipping cost is required")
    private BigDecimal shippingCost;

    @NotNull(message = "Recipient address is required")
    @Valid
    private ShippingAddressRequest recipientAddress;

    private LocalDateTime estimatedDeliveryDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddressRequest {
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
