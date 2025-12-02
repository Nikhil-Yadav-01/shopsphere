package com.rudraksha.shopsphere.shipping.service.impl;

import com.rudraksha.shopsphere.shipping.dto.request.CreateShipmentRequest;
import com.rudraksha.shopsphere.shipping.dto.request.UpdateTrackingRequest;
import com.rudraksha.shopsphere.shipping.dto.response.ShipmentResponse;
import com.rudraksha.shopsphere.shipping.dto.response.TrackingEventResponse;
import com.rudraksha.shopsphere.shipping.entity.Shipment;
import com.rudraksha.shopsphere.shipping.entity.ShippingAddress;
import com.rudraksha.shopsphere.shipping.entity.TrackingEvent;
import com.rudraksha.shopsphere.shipping.exception.InvalidShipmentStateException;
import com.rudraksha.shopsphere.shipping.exception.ShipmentNotFoundException;
import com.rudraksha.shopsphere.shipping.mapper.ShippingMapper;
import com.rudraksha.shopsphere.shipping.repository.ShipmentRepository;
import com.rudraksha.shopsphere.shipping.repository.TrackingEventRepository;
import com.rudraksha.shopsphere.shipping.service.ShippingService;
import com.rudraksha.shopsphere.shared.models.enums.ShipmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ShippingServiceImpl implements ShippingService {

    private final ShipmentRepository shipmentRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final ShippingMapper shippingMapper;

    @Override
    public ShipmentResponse createShipment(CreateShipmentRequest request) {
        log.info("Creating shipment for order: {}", request.getOrderId());

        // Generate unique tracking number
        String trackingNumber = "TRACK-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);

        // Create shipment
        ShippingAddress address = shippingMapper.toShippingAddress(request.getRecipientAddress());
        
        Shipment shipment = Shipment.builder()
                .orderId(request.getOrderId())
                .trackingNumber(trackingNumber)
                .status(ShipmentStatus.PENDING)
                .carrier(request.getCarrier())
                .shippingCost(request.getShippingCost())
                .estimatedDeliveryDate(request.getEstimatedDeliveryDate())
                .recipientAddress(address)
                .build();

        Shipment savedShipment = shipmentRepository.save(shipment);
        log.info("Shipment created with tracking number: {}", trackingNumber);

        return shippingMapper.toShipmentResponse(savedShipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(UUID shipmentId) {
        log.debug("Fetching shipment with ID: {}", shipmentId);

        Shipment shipment = shipmentRepository.findByIdWithEvents(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found with ID: " + shipmentId));

        return shippingMapper.toShipmentResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByOrderId(UUID orderId) {
        log.debug("Fetching shipment for order: {}", orderId);

        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found for order ID: " + orderId));

        return shippingMapper.toShipmentResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getShipmentsByStatus(String status, Pageable pageable) {
        log.debug("Fetching shipments with status: {}", status);

        try {
            ShipmentStatus shipmentStatus = ShipmentStatus.valueOf(status.toUpperCase());
            return shipmentRepository.findByStatus(shipmentStatus, pageable)
                    .map(shippingMapper::toShipmentResponse);
        } catch (IllegalArgumentException e) {
            throw new InvalidShipmentStateException("Invalid shipment status: " + status);
        }
    }

    @Override
    public ShipmentResponse updateTracking(UUID shipmentId, UpdateTrackingRequest request) {
        log.info("Updating tracking for shipment: {}", shipmentId);

        Shipment shipment = shipmentRepository.findByIdWithEvents(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found with ID: " + shipmentId));

        // Add tracking event
        TrackingEvent event = TrackingEvent.builder()
                .eventCode(request.getEventCode())
                .description(request.getDescription())
                .location(request.getLocation())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        shipment.addTrackingEvent(event);

        // Update shipment status based on event code
        if ("DELIVERED".equalsIgnoreCase(request.getEventCode())) {
            shipment.setStatus(ShipmentStatus.DELIVERED);
            shipment.setActualDeliveryDate(LocalDateTime.now());
        } else if ("IN_TRANSIT".equalsIgnoreCase(request.getEventCode())) {
            shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        } else if ("OUT_FOR_DELIVERY".equalsIgnoreCase(request.getEventCode())) {
            shipment.setStatus(ShipmentStatus.OUT_FOR_DELIVERY);
        }

        Shipment updatedShipment = shipmentRepository.save(shipment);
        log.info("Tracking updated for shipment: {}", shipmentId);

        return shippingMapper.toShipmentResponse(updatedShipment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingEventResponse> getTrackingEvents(UUID shipmentId) {
        log.debug("Fetching tracking events for shipment: {}", shipmentId);

        // Verify shipment exists
        if (!shipmentRepository.existsById(shipmentId)) {
            throw new ShipmentNotFoundException("Shipment not found with ID: " + shipmentId);
        }

        List<TrackingEvent> events = trackingEventRepository.findByShipmentIdOrderByOccurredAtDesc(shipmentId);
        return shippingMapper.toTrackingEventResponseList(events);
    }

    @Override
    public ShipmentResponse markAsDelivered(UUID shipmentId) {
        log.info("Marking shipment as delivered: {}", shipmentId);

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found with ID: " + shipmentId));

        if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
            throw new InvalidShipmentStateException("Shipment is already marked as delivered");
        }

        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setActualDeliveryDate(LocalDateTime.now());

        Shipment updatedShipment = shipmentRepository.save(shipment);
        log.info("Shipment marked as delivered: {}", shipmentId);

        return shippingMapper.toShipmentResponse(updatedShipment);
    }
}
