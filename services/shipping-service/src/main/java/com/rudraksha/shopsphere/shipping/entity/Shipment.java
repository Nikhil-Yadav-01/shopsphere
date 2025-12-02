package com.rudraksha.shopsphere.shipping.entity;

import com.rudraksha.shopsphere.shared.models.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "tracking_number", unique = true, nullable = false, length = 50)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ShipmentStatus status = ShipmentStatus.PENDING;

    @Column(name = "carrier", nullable = false, length = 50)
    private String carrier;

    @Column(name = "shipping_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fullName", column = @Column(name = "recipient_full_name")),
            @AttributeOverride(name = "phone", column = @Column(name = "recipient_phone")),
            @AttributeOverride(name = "addressLine1", column = @Column(name = "recipient_address_line1")),
            @AttributeOverride(name = "addressLine2", column = @Column(name = "recipient_address_line2")),
            @AttributeOverride(name = "city", column = @Column(name = "recipient_city")),
            @AttributeOverride(name = "state", column = @Column(name = "recipient_state")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "recipient_postal_code")),
            @AttributeOverride(name = "country", column = @Column(name = "recipient_country"))
    })
    private ShippingAddress recipientAddress;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TrackingEvent> trackingEvents = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addTrackingEvent(TrackingEvent event) {
        trackingEvents.add(event);
        event.setShipment(this);
    }

    public void removeTrackingEvent(TrackingEvent event) {
        trackingEvents.remove(event);
        event.setShipment(null);
    }
}
