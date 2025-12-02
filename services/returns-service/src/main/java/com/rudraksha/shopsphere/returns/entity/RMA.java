package com.rudraksha.shopsphere.returns.entity;

import com.rudraksha.shopsphere.shared.models.enums.ReturnStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rmas", indexes = {
    @Index(name = "idx_rma_number", columnList = "rma_number", unique = true),
    @Index(name = "idx_rma_return_request_id", columnList = "return_request_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RMA {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "return_request_id", nullable = false)
    private UUID returnRequestId;

    @Column(name = "rma_number", unique = true, nullable = false, length = 50)
    private String rmaNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReturnStatus status = ReturnStatus.REQUESTED;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "shipping_label_url")
    private String shippingLabelUrl;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "is_expired", nullable = false)
    @Builder.Default
    private Boolean isExpired = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean hasExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public void markAsExpired() {
        this.isExpired = true;
    }
}
