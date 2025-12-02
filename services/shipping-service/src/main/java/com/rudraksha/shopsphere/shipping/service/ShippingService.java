package com.rudraksha.shopsphere.shipping.service;

import com.rudraksha.shopsphere.shipping.dto.request.CreateShipmentRequest;
import com.rudraksha.shopsphere.shipping.dto.request.UpdateTrackingRequest;
import com.rudraksha.shopsphere.shipping.dto.response.ShipmentResponse;
import com.rudraksha.shopsphere.shipping.dto.response.TrackingEventResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ShippingService {

    /**
     * Creates a new shipment
     */
    ShipmentResponse createShipment(CreateShipmentRequest request);

    /**
     * Retrieves a shipment by ID
     */
    ShipmentResponse getShipmentById(UUID shipmentId);

    /**
     * Retrieves a shipment by order ID
     */
    ShipmentResponse getShipmentByOrderId(UUID orderId);

    /**
     * Lists shipments by status with pagination
     */
    Page<ShipmentResponse> getShipmentsByStatus(String status, Pageable pageable);

    /**
     * Updates tracking information for a shipment
     */
    ShipmentResponse updateTracking(UUID shipmentId, UpdateTrackingRequest request);

    /**
     * Gets all tracking events for a shipment
     */
    List<TrackingEventResponse> getTrackingEvents(UUID shipmentId);

    /**
     * Marks shipment as delivered
     */
    ShipmentResponse markAsDelivered(UUID shipmentId);
}
