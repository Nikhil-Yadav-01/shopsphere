package com.rudraksha.shopsphere.shipping.mapper;

import com.rudraksha.shopsphere.shipping.dto.request.CreateShipmentRequest;
import com.rudraksha.shopsphere.shipping.dto.response.ShipmentResponse;
import com.rudraksha.shopsphere.shipping.dto.response.TrackingEventResponse;
import com.rudraksha.shopsphere.shipping.entity.Shipment;
import com.rudraksha.shopsphere.shipping.entity.ShippingAddress;
import com.rudraksha.shopsphere.shipping.entity.TrackingEvent;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShippingMapper {

    private final ModelMapper modelMapper;

    public ShippingMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public ShipmentResponse toShipmentResponse(Shipment shipment) {
        return modelMapper.map(shipment, ShipmentResponse.class);
    }

    public List<ShipmentResponse> toShipmentResponseList(List<Shipment> shipments) {
        return shipments.stream()
                .map(this::toShipmentResponse)
                .collect(Collectors.toList());
    }

    public TrackingEventResponse toTrackingEventResponse(TrackingEvent trackingEvent) {
        return modelMapper.map(trackingEvent, TrackingEventResponse.class);
    }

    public List<TrackingEventResponse> toTrackingEventResponseList(List<TrackingEvent> trackingEvents) {
        return trackingEvents.stream()
                .map(this::toTrackingEventResponse)
                .collect(Collectors.toList());
    }

    public ShipmentResponse.AddressResponse toAddressResponse(ShippingAddress address) {
        if (address == null) {
            return null;
        }
        return modelMapper.map(address, ShipmentResponse.AddressResponse.class);
    }

    public ShippingAddress toShippingAddress(CreateShipmentRequest.ShippingAddressRequest addressRequest) {
        if (addressRequest == null) {
            return null;
        }
        return modelMapper.map(addressRequest, ShippingAddress.class);
    }
}
