package com.rudraksha.shopsphere.user.dto.response;

import com.rudraksha.shopsphere.user.entity.Address.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {

    private UUID id;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefault;
    private AddressType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
