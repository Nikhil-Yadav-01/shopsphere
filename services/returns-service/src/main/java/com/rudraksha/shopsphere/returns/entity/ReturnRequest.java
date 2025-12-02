package com.rudraksha.shopsphere.returns.entity;

import com.rudraksha.shopsphere.shared.models.enums.ReturnStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "return_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "rma_number", unique = true, nullable = false, length = 50)
    private String rmaNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReturnStatus status = ReturnStatus.REQUESTED;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "shipping_cost_deductible", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingCostDeductible = BigDecimal.ZERO;

    @Column(name = "final_refund_amount", precision = 10, scale = 2)
    private BigDecimal finalRefundAmount;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "return_received_at")
    private LocalDateTime returnReceivedAt;

    @Column(name = "refund_processed_at")
    private LocalDateTime refundProcessedAt;

    @Column(name = "refund_transaction_id")
    private String refundTransactionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void approve() {
        this.status = ReturnStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject(String reason) {
        this.status = ReturnStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
        this.rejectionReason = reason;
    }

    public void markAsReceived() {
        this.status = ReturnStatus.RECEIVED;
        this.returnReceivedAt = LocalDateTime.now();
    }

    public void processRefund(String transactionId, BigDecimal amount) {
        this.status = ReturnStatus.REFUNDED;
        this.refundTransactionId = transactionId;
        this.finalRefundAmount = amount;
        this.refundProcessedAt = LocalDateTime.now();
    }
}
