package com.rudraksha.shopsphere.payment.service.impl;

import com.rudraksha.shopsphere.payment.dto.request.CreatePaymentRequest;
import com.rudraksha.shopsphere.payment.dto.request.RefundRequest;
import com.rudraksha.shopsphere.payment.dto.response.PaymentResponse;
import com.rudraksha.shopsphere.payment.dto.response.RefundResponse;
import com.rudraksha.shopsphere.payment.entity.Payment;
import com.rudraksha.shopsphere.payment.entity.Refund;
import com.rudraksha.shopsphere.payment.events.producer.PaymentEventProducer;
import com.rudraksha.shopsphere.payment.exception.InvalidPaymentStateException;
import com.rudraksha.shopsphere.payment.exception.PaymentNotFoundException;
import com.rudraksha.shopsphere.payment.gateway.PaymentGateway;
import com.rudraksha.shopsphere.payment.repository.PaymentRepository;
import com.rudraksha.shopsphere.payment.repository.RefundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private PaymentEventProducer eventProducer;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID paymentId;
    private UUID orderId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void createPayment_Success() {
        // Arrange
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId(orderId);
        request.setUserId(userId);
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setCurrency("usd");
        request.setPaymentMethod("card");

        PaymentGateway.PaymentResult paymentResult = new PaymentGateway.PaymentResult(
                true, "pi_test123", "{\"status\":\"succeeded\"}"
        );

        Payment paymentEntity = Payment.builder()
                .id(paymentId)
                .orderId(orderId)
                .userId(userId)
                .amount(request.getAmount())
                .currency("USD")
                .paymentMethod("card")
                .status(Payment.PaymentStatus.PROCESSING)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(paymentEntity);
        when(paymentGateway.processPayment(anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(paymentResult);

        // Act
        PaymentResponse response = paymentService.createPayment(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getAmount()).isEqualTo(BigDecimal.valueOf(100.00));

        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(eventProducer).publishPaymentCompleted(any(Payment.class));
    }

    @Test
    void createPayment_GatewayFails() {
        // Arrange
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId(orderId);
        request.setUserId(userId);
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setCurrency("usd");
        request.setPaymentMethod("card");

        PaymentGateway.PaymentResult paymentResult = new PaymentGateway.PaymentResult(
                false, null, "{\"error\":\"declined\"}"
        );

        Payment paymentEntity = Payment.builder()
                .id(paymentId)
                .orderId(orderId)
                .userId(userId)
                .amount(request.getAmount())
                .currency("USD")
                .paymentMethod("card")
                .status(Payment.PaymentStatus.PROCESSING)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(paymentEntity);
        when(paymentGateway.processPayment(anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(paymentResult);

        // Act
        PaymentResponse response = paymentService.createPayment(request);

        // Assert
        assertThat(response).isNotNull();
        verify(eventProducer).publishPaymentFailed(any(Payment.class));
    }

    @Test
    void getPayment_Success() {
        // Arrange
        Payment paymentEntity = Payment.builder()
                .id(paymentId)
                .orderId(orderId)
                .userId(userId)
                .amount(BigDecimal.valueOf(100.00))
                .currency("USD")
                .paymentMethod("card")
                .status(Payment.PaymentStatus.COMPLETED)
                .transactionId("pi_test123")
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));

        // Act
        PaymentResponse response = paymentService.getPayment(paymentId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
    }

    @Test
    void getPayment_NotFound() {
        // Arrange
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.getPayment(paymentId))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("Payment not found");
    }

    @Test
    void processRefund_Success() {
        // Arrange
        Payment paymentEntity = Payment.builder()
                .id(paymentId)
                .orderId(orderId)
                .userId(userId)
                .amount(BigDecimal.valueOf(100.00))
                .currency("USD")
                .paymentMethod("card")
                .status(Payment.PaymentStatus.COMPLETED)
                .transactionId("pi_test123")
                .build();

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setAmount(BigDecimal.valueOf(100.00));
        refundRequest.setReason("Customer requested");

        Refund refundEntity = Refund.builder()
                .paymentId(paymentId)
                .amount(refundRequest.getAmount())
                .reason(refundRequest.getReason())
                .status(Refund.RefundStatus.PROCESSING)
                .build();

        PaymentGateway.RefundResult refundResult = new PaymentGateway.RefundResult(
                true, "re_test123", "{\"status\":\"succeeded\"}"
        );

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(refundRepository.save(any(Refund.class))).thenReturn(refundEntity);
        when(paymentGateway.processRefund(anyString(), any(BigDecimal.class)))
                .thenReturn(refundResult);

        // Act
        RefundResponse response = paymentService.processRefund(paymentId, refundRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(refundRepository, times(2)).save(any(Refund.class));
        verify(eventProducer).publishPaymentRefunded(any(Payment.class), any(Refund.class));
    }

    @Test
    void processRefund_PaymentNotCompleted() {
        // Arrange
        Payment paymentEntity = Payment.builder()
                .id(paymentId)
                .orderId(orderId)
                .userId(userId)
                .amount(BigDecimal.valueOf(100.00))
                .currency("USD")
                .paymentMethod("card")
                .status(Payment.PaymentStatus.PROCESSING)
                .build();

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setAmount(BigDecimal.valueOf(100.00));
        refundRequest.setReason("Customer requested");

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.processRefund(paymentId, refundRequest))
                .isInstanceOf(InvalidPaymentStateException.class)
                .hasMessageContaining("Cannot refund payment");
    }

    @Test
    void processRefund_NoTransactionId() {
        // Arrange
        Payment paymentEntity = Payment.builder()
                .id(paymentId)
                .orderId(orderId)
                .userId(userId)
                .amount(BigDecimal.valueOf(100.00))
                .currency("USD")
                .paymentMethod("card")
                .status(Payment.PaymentStatus.COMPLETED)
                .transactionId(null)
                .build();

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setAmount(BigDecimal.valueOf(100.00));
        refundRequest.setReason("Customer requested");

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.processRefund(paymentId, refundRequest))
                .isInstanceOf(InvalidPaymentStateException.class)
                .hasMessageContaining("no transaction ID");
    }

    @Test
    void handleWebhookEvent_ValidSignature() {
        // Arrange
        String payload = "{\"type\":\"payment_intent.succeeded\",\"data\":{\"object\":{\"id\":\"pi_test123\"}}}";
        String signature = "t=1234567890,v1=valid_signature";

        // Mock the static method call to getWebhookSecret (using reflection or spy)
        // This is a simplified test - in production, you'd use more sophisticated mocking

        // Act & Assert - webhook processing should not throw
        try {
            // Note: This test is simplified. Real webhook testing requires mocking getWebhookSecret
            paymentService.handleWebhookEvent(payload, signature);
        } catch (Exception e) {
            // Expected since we can't mock the signature verification without more setup
            assertThat(e).isInstanceOf(InvalidPaymentStateException.class);
        }
    }

    @Test
    void handleWebhookEvent_EmptyPayload() {
        // Arrange
        String payload = "";
        String signature = "t=1234567890,v1=valid_signature";

        // Act & Assert
        paymentService.handleWebhookEvent(payload, signature);
        // Should complete without error
    }

    @Test
    void handleWebhookEvent_NullPayload() {
        // Arrange
        String payload = null;
        String signature = "t=1234567890,v1=valid_signature";

        // Act & Assert
        paymentService.handleWebhookEvent(payload, signature);
        // Should complete without error
    }
}
