package com.rudraksha.shopsphere.shipping.controller;

import com.rudraksha.shopsphere.shipping.dto.request.CreateShipmentRequest;
import com.rudraksha.shopsphere.shipping.dto.request.UpdateTrackingRequest;
import com.rudraksha.shopsphere.shipping.dto.response.ShipmentResponse;
import com.rudraksha.shopsphere.shipping.dto.response.TrackingEventResponse;
import com.rudraksha.shopsphere.shipping.service.ShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
@Slf4j
public class ShippingController {

    private final ShippingService shippingService;

    /**
     * Create a new shipment
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody CreateShipmentRequest request) {
        log.info("POST /api/v1/shipments - Creating new shipment for order: {}", request.getOrderId());
        ShipmentResponse response = shippingService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get shipment by ID
     */
    @GetMapping("/{shipmentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ShipmentResponse> getShipment(@PathVariable UUID shipmentId) {
        log.info("GET /api/v1/shipments/{} - Fetching shipment", shipmentId);
        ShipmentResponse response = shippingService.getShipmentById(shipmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get shipment by order ID
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ShipmentResponse> getShipmentByOrder(@PathVariable UUID orderId) {
        log.info("GET /api/v1/shipments/order/{} - Fetching shipment for order", orderId);
        ShipmentResponse response = shippingService.getShipmentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get shipments by status
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ShipmentResponse>> getShipmentsByStatus(
            @RequestParam String status,
            Pageable pageable) {
        log.info("GET /api/v1/shipments - Fetching shipments with status: {}", status);
        Page<ShipmentResponse> response = shippingService.getShipmentsByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Update tracking information
     */
    @PutMapping("/{shipmentId}/track")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShipmentResponse> updateTracking(
            @PathVariable UUID shipmentId,
            @Valid @RequestBody UpdateTrackingRequest request) {
        log.info("PUT /api/v1/shipments/{}/track - Updating tracking", shipmentId);
        ShipmentResponse response = shippingService.updateTracking(shipmentId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get tracking events for a shipment
     */
    @GetMapping("/{shipmentId}/tracking")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<TrackingEventResponse>> getTrackingEvents(@PathVariable UUID shipmentId) {
        log.info("GET /api/v1/shipments/{}/tracking - Fetching tracking events", shipmentId);
        List<TrackingEventResponse> response = shippingService.getTrackingEvents(shipmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark shipment as delivered
     */
    @PutMapping("/{shipmentId}/delivered")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShipmentResponse> markAsDelivered(@PathVariable UUID shipmentId) {
        log.info("PUT /api/v1/shipments/{}/delivered - Marking shipment as delivered", shipmentId);
        ShipmentResponse response = shippingService.markAsDelivered(shipmentId);
        return ResponseEntity.ok(response);
    }
}
