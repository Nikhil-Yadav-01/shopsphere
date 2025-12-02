package com.rudraksha.shopsphere.shipping.mapper;

import com.rudraksha.shopsphere.shipping.dto.request.CreateShipmentRequest;
import com.rudraksha.shopsphere.shipping.dto.response.ShipmentResponse;
import com.rudraksha.shopsphere.shipping.dto.response.TrackingEventResponse;
import com.rudraksha.shopsphere.shipping.entity.Shipment;
import com.rudraksha.shopsphere.shipping.entity.ShippingAddress;
import com.rudraksha.shopsphere.shipping.entity.TrackingEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShippingMapper {

    ShipmentResponse toShipmentResponse(Shipment shipment);

    List<ShipmentResponse> toShipmentResponseList(List<Shipment> shipments);

    TrackingEventResponse toTrackingEventResponse(TrackingEvent trackingEvent);

    List<TrackingEventResponse> toTrackingEventResponseList(List<TrackingEvent> trackingEvents);

    @Named("toAddressResponse")
    default ShipmentResponse.AddressResponse toAddressResponse(ShippingAddress address) {
        if (address == null) {
            return null;
        }
        return ShipmentResponse.AddressResponse.builder()
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .build();
    }

    default ShippingAddress toShippingAddress(CreateShipmentRequest.ShippingAddressRequest addressRequest) {
        if (addressRequest == null) {
            return null;
        }
        return ShippingAddress.builder()
                .fullName(addressRequest.getFullName())
                .phone(addressRequest.getPhone())
                .addressLine1(addressRequest.getAddressLine1())
                .addressLine2(addressRequest.getAddressLine2())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .postalCode(addressRequest.getPostalCode())
                .country(addressRequest.getCountry())
                .build();
    }
}
