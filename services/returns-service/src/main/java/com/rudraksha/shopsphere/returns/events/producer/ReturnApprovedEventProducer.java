package com.rudraksha.shopsphere.returns.events.producer;

import com.rudraksha.shopsphere.returns.dto.response.ReturnResponse;
import com.rudraksha.shopsphere.shared.kafka.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReturnApprovedEventProducer {

    private final EventPublisher eventPublisher;
    private static final String TOPIC = "return.approved";

    public void publishReturnApproved(ReturnResponse returnResponse) {
        log.info("Publishing return approved event for RMA: {}", returnResponse.getRmaNumber());

        try {
            eventPublisher.publish(TOPIC, returnResponse.getId().toString(), returnResponse);
        } catch (Exception e) {
            log.error("Failed to publish return approved event for RMA: {}", returnResponse.getRmaNumber(), e);
            throw new RuntimeException("Failed to publish return approved event", e);
        }
    }
}
