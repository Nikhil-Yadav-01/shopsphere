package com.rudraksha.shopsphere.shipping.service;

import com.rudraksha.shopsphere.shared.models.enums.ShipmentStatus;
import com.rudraksha.shopsphere.shipping.dto.request.CreateShipmentRequest;
import com.rudraksha.shopsphere.shipping.dto.request.UpdateTrackingRequest;
import com.rudraksha.shopsphere.shipping.dto.response.ShipmentResponse;
import com.rudraksha.shopsphere.shipping.entity.Shipment;
import com.rudraksha.shopsphere.shipping.entity.TrackingEvent;
import com.rudraksha.shopsphere.shipping.exception.InvalidShipmentStateException;
import com.rudraksha.shopsphere.shipping.exception.ShipmentNotFoundException;
import com.rudraksha.shopsphere.shipping.mapper.ShippingMapper;
import com.rudraksha.shopsphere.shipping.repository.ShipmentRepository;
import com.rudraksha.shopsphere.shipping.repository.TrackingEventRepository;
import com.rudraksha.shopsphere.shipping.service.impl.ShippingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingServiceImplTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private TrackingEventRepository trackingEventRepository;

    @Mock
    private ShippingMapper shippingMapper;

    private ShippingService shippingService;

    @BeforeEach
    void setUp() {
        shippingService = new ShippingServiceImpl(shipmentRepository, trackingEventRepository, shippingMapper);
    }

    private CreateShipmentRequest createValidShipmentRequest() {
        CreateShipmentRequest.ShippingAddressRequest address =
                CreateShipmentRequest.ShippingAddressRequest.builder()
                        .fullName("John Doe")
                        .phone("555-1234")
                        .addressLine1("123 Main St")
                        .city("New York")
                        .state("NY")
                        .postalCode("10001")
                        .country("USA")
                        .build();

        return CreateShipmentRequest.builder()
                .orderId(UUID.randomUUID())
                .carrier("FedEx")
                .shippingCost(new BigDecimal("25.00"))
                .recipientAddress(address)
                .estimatedDeliveryDate(LocalDateTime.now().plusDays(5))
                .build();
    }

    @Test
    void testCreateShipment_Success() {
        // Arrange
        CreateShipmentRequest request = createValidShipmentRequest();
        Shipment mockShipment = new Shipment();
        mockShipment.setId(UUID.randomUUID());
        mockShipment.setTrackingNumber("TRACK-12345");
        mockShipment.setStatus(ShipmentStatus.PENDING);
        mockShipment.setTrackingEvents(new ArrayList<>());

        ShipmentResponse expectedResponse = ShipmentResponse.builder()
                .id(mockShipment.getId())
                .trackingNumber("TRACK-12345")
                .status(ShipmentStatus.PENDING)
                .build();

        when(shipmentRepository.save(any(Shipment.class))).thenReturn(mockShipment);
        when(shippingMapper.toShipmentResponse(mockShipment)).thenReturn(expectedResponse);

        // Act
        ShipmentResponse result = shippingService.createShipment(request);

        // Assert
        assertNotNull(result);
        assertEquals("TRACK-12345", result.getTrackingNumber());
        assertEquals(ShipmentStatus.PENDING, result.getStatus());
        verify(shipmentRepository, times(1)).save(any(Shipment.class));
    }

    @Test
    void testGetShipmentById_Success() {
        // Arrange
        UUID shipmentId = UUID.randomUUID();
        Shipment mockShipment = Shipment.builder()
                .id(shipmentId)
                .trackingNumber("TRACK-12345")
                .status(ShipmentStatus.IN_TRANSIT)
                .build();

        ShipmentResponse expectedResponse = ShipmentResponse.builder()
                .id(shipmentId)
                .trackingNumber("TRACK-12345")
                .status(ShipmentStatus.IN_TRANSIT)
                .build();

        when(shipmentRepository.findByIdWithEvents(shipmentId)).thenReturn(Optional.of(mockShipment));
        when(shippingMapper.toShipmentResponse(mockShipment)).thenReturn(expectedResponse);

        // Act
        ShipmentResponse result = shippingService.getShipmentById(shipmentId);

        // Assert
        assertNotNull(result);
        assertEquals("TRACK-12345", result.getTrackingNumber());
        verify(shipmentRepository, times(1)).findByIdWithEvents(shipmentId);
    }

    @Test
    void testGetShipmentById_NotFound_ThrowsException() {
        // Arrange
        UUID shipmentId = UUID.randomUUID();
        when(shipmentRepository.findByIdWithEvents(shipmentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ShipmentNotFoundException.class, () -> shippingService.getShipmentById(shipmentId));
    }

    @Test
    void testGetShipmentByOrderId_Success() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Shipment mockShipment = Shipment.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .trackingNumber("TRACK-12345")
                .status(ShipmentStatus.PENDING)
                .build();

        ShipmentResponse expectedResponse = ShipmentResponse.builder()
                .orderId(orderId)
                .trackingNumber("TRACK-12345")
                .status(ShipmentStatus.PENDING)
                .build();

        when(shipmentRepository.findByOrderId(orderId)).thenReturn(Optional.of(mockShipment));
        when(shippingMapper.toShipmentResponse(mockShipment)).thenReturn(expectedResponse);

        // Act
        ShipmentResponse result = shippingService.getShipmentByOrderId(orderId);

        // Assert
        assertNotNull(result);
        assertEquals("TRACK-12345", result.getTrackingNumber());
        verify(shipmentRepository, times(1)).findByOrderId(orderId);
    }

    @Test
    void testGetShipmentsByStatus_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Shipment shipment = Shipment.builder()
                .id(UUID.randomUUID())
                .trackingNumber("TRACK-123")
                .status(ShipmentStatus.IN_TRANSIT)
                .build();

        Page<Shipment> shipmentsPage = new PageImpl<>(List.of(shipment), pageable, 1);
        ShipmentResponse response = ShipmentResponse.builder()
                .trackingNumber("TRACK-123")
                .status(ShipmentStatus.IN_TRANSIT)
                .build();

        when(shipmentRepository.findByStatus(ShipmentStatus.IN_TRANSIT, pageable)).thenReturn(shipmentsPage);
        when(shippingMapper.toShipmentResponse(shipment)).thenReturn(response);

        // Act
        Page<ShipmentResponse> result = shippingService.getShipmentsByStatus("IN_TRANSIT", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(shipmentRepository, times(1)).findByStatus(ShipmentStatus.IN_TRANSIT, pageable);
    }

    @Test
    void testUpdateTracking_Success() {
        // Arrange
        UUID shipmentId = UUID.randomUUID();
        Shipment shipment = Shipment.builder()
                .id(shipmentId)
                .trackingNumber("TRACK-123")
                .status(ShipmentStatus.PENDING)
                .trackingEvents(new ArrayList<>())
                .build();

        UpdateTrackingRequest request = UpdateTrackingRequest.builder()
                .eventCode("IN_TRANSIT")
                .description("Package is in transit")
                .location("New York, NY")
                .build();

        ShipmentResponse expectedResponse = ShipmentResponse.builder()
                .id(shipmentId)
                .trackingNumber("TRACK-123")
                .status(ShipmentStatus.IN_TRANSIT)
                .build();

        when(shipmentRepository.findByIdWithEvents(shipmentId)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
        when(shippingMapper.toShipmentResponse(any(Shipment.class))).thenReturn(expectedResponse);

        // Act
        ShipmentResponse result = shippingService.updateTracking(shipmentId, request);

        // Assert
        assertNotNull(result);
        assertEquals(ShipmentStatus.IN_TRANSIT, result.getStatus());
        verify(shipmentRepository, times(1)).findByIdWithEvents(shipmentId);
    }

    @Test
    void testGetTrackingEvents_Success() {
        // Arrange
        UUID shipmentId = UUID.randomUUID();
        TrackingEvent event = TrackingEvent.builder()
                .id(UUID.randomUUID())
                .eventCode("IN_TRANSIT")
                .description("Package is in transit")
                .build();

        when(shipmentRepository.existsById(shipmentId)).thenReturn(true);
        when(trackingEventRepository.findByShipmentIdOrderByOccurredAtDesc(shipmentId)).thenReturn(List.of(event));
        when(shippingMapper.toTrackingEventResponseList(any())).thenReturn(List.of());

        // Act
        List<?> result = shippingService.getTrackingEvents(shipmentId);

        // Assert
        assertNotNull(result);
        verify(trackingEventRepository, times(1)).findByShipmentIdOrderByOccurredAtDesc(shipmentId);
    }

    @Test
    void testMarkAsDelivered_Success() {
        // Arrange
        UUID shipmentId = UUID.randomUUID();
        Shipment shipment = Shipment.builder()
                .id(shipmentId)
                .trackingNumber("TRACK-123")
                .status(ShipmentStatus.OUT_FOR_DELIVERY)
                .build();

        ShipmentResponse expectedResponse = ShipmentResponse.builder()
                .id(shipmentId)
                .trackingNumber("TRACK-123")
                .status(ShipmentStatus.DELIVERED)
                .build();

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
        when(shippingMapper.toShipmentResponse(any(Shipment.class))).thenReturn(expectedResponse);

        // Act
        ShipmentResponse result = shippingService.markAsDelivered(shipmentId);

        // Assert
        assertNotNull(result);
        assertEquals(ShipmentStatus.DELIVERED, result.getStatus());
        verify(shipmentRepository, times(1)).findById(shipmentId);
    }

    @Test
    void testMarkAsDelivered_AlreadyDelivered_ThrowsException() {
        // Arrange
        UUID shipmentId = UUID.randomUUID();
        Shipment shipment = Shipment.builder()
                .id(shipmentId)
                .trackingNumber("TRACK-123")
                .status(ShipmentStatus.DELIVERED)
                .build();

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));

        // Act & Assert
        assertThrows(InvalidShipmentStateException.class, () -> shippingService.markAsDelivered(shipmentId));
    }
}
