package com.rudraksha.shopsphere.payment.events.producer;

import com.rudraksha.shopsphere.payment.entity.Payment;
import com.rudraksha.shopsphere.payment.entity.Refund;
import com.rudraksha.shopsphere.shared.kafka.EventPublisher;
import com.rudraksha.shopsphere.shared.kafka.TopicConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final EventPublisher eventPublisher;

    public void publishPaymentCompleted(Payment payment) {
        Map<String, Object> event = createPaymentEvent("PAYMENT_COMPLETED", payment);
        eventPublisher.publish(TopicConstants.PAYMENT_EVENTS, payment.getOrderId().toString(), event);
        log.info("Published PAYMENT_COMPLETED event for paymentId={}", payment.getId());
    }

    public void publishPaymentFailed(Payment payment) {
        Map<String, Object> event = createPaymentEvent("PAYMENT_FAILED", payment);
        eventPublisher.publish(TopicConstants.PAYMENT_EVENTS, payment.getOrderId().toString(), event);
        log.info("Published PAYMENT_FAILED event for paymentId={}", payment.getId());
    }

    public void publishPaymentRefunded(Payment payment, Refund refund) {
        Map<String, Object> event = createPaymentEvent("PAYMENT_REFUNDED", payment);
        event.put("refundId", refund.getId().toString());
        event.put("refundAmount", refund.getAmount());
        event.put("refundReason", refund.getReason());
        eventPublisher.publish(TopicConstants.PAYMENT_EVENTS, payment.getOrderId().toString(), event);
        log.info("Published PAYMENT_REFUNDED event for paymentId={}, refundId={}", payment.getId(), refund.getId());
    }

    private Map<String, Object> createPaymentEvent(String eventType, Payment payment) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("paymentId", payment.getId().toString());
        event.put("orderId", payment.getOrderId().toString());
        event.put("userId", payment.getUserId().toString());
        event.put("amount", payment.getAmount());
        event.put("currency", payment.getCurrency());
        event.put("status", payment.getStatus().name());
        event.put("transactionId", payment.getTransactionId());
        event.put("timestamp", LocalDateTime.now().toString());
        return event;
    }
}
