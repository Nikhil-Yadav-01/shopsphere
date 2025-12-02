package com.rudraksha.shopsphere.returns.events.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to ShipmentDelivered events to enable return eligibility
 * When a shipment is delivered, customers become eligible to initiate returns
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ShipmentDeliveredConsumer {

    private static final String TOPIC = "shipment.delivered";
    private static final String GROUP_ID = "returns-service-shipment-delivered-group";

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumeShipmentDelivered(String message) {
        log.info("Received shipment delivered event: {}", message);

        try {
            // TODO: Parse message and update return eligibility
            // This event is used to know when an order has been delivered
            // and the return window starts
            log.debug("Processing shipment delivered for returns eligibility");
        } catch (Exception e) {
            log.error("Error processing shipment delivered event: {}", message, e);
            // Send to DLQ on failure
        }
    }
}
