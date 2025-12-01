package com.rudraksha.shopsphere.payment.service.impl;

import com.rudraksha.shopsphere.payment.dto.request.CreatePaymentRequest;
import com.rudraksha.shopsphere.payment.dto.request.RefundRequest;
import com.rudraksha.shopsphere.payment.dto.response.PaymentResponse;
import com.rudraksha.shopsphere.payment.dto.response.RefundResponse;
import com.rudraksha.shopsphere.payment.entity.Payment;
import com.rudraksha.shopsphere.payment.entity.Refund;
import com.rudraksha.shopsphere.payment.events.producer.PaymentEventProducer;
import com.rudraksha.shopsphere.payment.exception.PaymentNotFoundException;
import com.rudraksha.shopsphere.payment.exception.InvalidPaymentStateException;
import com.rudraksha.shopsphere.payment.gateway.PaymentGateway;
import com.rudraksha.shopsphere.payment.repository.PaymentRepository;
import com.rudraksha.shopsphere.payment.repository.RefundRepository;
import com.rudraksha.shopsphere.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentEventProducer eventProducer;

    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        log.info("Creating payment for orderId={}", request.getOrderId());

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency().toUpperCase())
                .paymentMethod(request.getPaymentMethod())
                .status(Payment.PaymentStatus.PROCESSING)
                .build();

        payment = paymentRepository.save(payment);

        PaymentGateway.PaymentResult result = paymentGateway.processPayment(
                request.getPaymentMethod(),
                request.getAmount(),
                request.getCurrency()
        );

        if (result.success()) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setTransactionId(result.transactionId());
            payment.setGatewayResponse(result.gatewayResponse());
            eventProducer.publishPaymentCompleted(payment);
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setGatewayResponse(result.gatewayResponse());
            eventProducer.publishPaymentFailed(payment);
        }

        payment = paymentRepository.save(payment);
        log.info("Payment created: id={}, status={}", payment.getId(), payment.getStatus());

        return PaymentResponse.fromEntity(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        return PaymentResponse.fromEntity(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .map(PaymentResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public RefundResponse processRefund(UUID paymentId, RefundRequest request) {
        log.info("Processing refund for paymentId={}, amount={}", paymentId, request.getAmount());

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStateException(
                    "Cannot refund payment with status: " + payment.getStatus(),
                    payment.getStatus().toString(),
                    "REFUND");
        }

        if (payment.getTransactionId() == null) {
            throw new InvalidPaymentStateException(
                    "Payment has no transaction ID for refund",
                    payment.getStatus().toString(),
                    "REFUND");
        }

        Refund refund = Refund.builder()
                .paymentId(paymentId)
                .amount(request.getAmount())
                .reason(request.getReason())
                .status(Refund.RefundStatus.PROCESSING)
                .build();

        refund = refundRepository.save(refund);

        PaymentGateway.RefundResult result = paymentGateway.processRefund(
                payment.getTransactionId(),
                request.getAmount()
        );

        if (result != null && result.success()) {
            refund.setStatus(Refund.RefundStatus.COMPLETED);
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            eventProducer.publishPaymentRefunded(payment, refund);
        } else {
            refund.setStatus(Refund.RefundStatus.FAILED);
        }

        refund = refundRepository.save(refund);
        log.info("Refund processed: id={}, status={}", refund.getId(), refund.getStatus());

        return RefundResponse.fromEntity(refund);
    }

    @Override
    @Transactional
    public void handleWebhookEvent(String payload, String signature) {
        log.info("Processing webhook event");

        log.info("Webhook event processed successfully");
    }
}
