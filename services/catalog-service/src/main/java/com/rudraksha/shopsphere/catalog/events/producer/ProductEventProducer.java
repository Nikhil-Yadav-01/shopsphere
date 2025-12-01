package com.rudraksha.shopsphere.catalog.events.producer;

import com.rudraksha.shopsphere.catalog.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.product-created:product-created}")
    private String productCreatedTopic;

    @Value("${kafka.topics.product-updated:product-updated}")
    private String productUpdatedTopic;

    public void publishProductCreated(Product product) {
        Map<String, Object> event = buildProductEvent(product, "PRODUCT_CREATED");
        kafkaTemplate.send(productCreatedTopic, product.getId(), event);
        log.info("Published product created event for product ID: {}", product.getId());
    }

    public void publishProductUpdated(Product product) {
        Map<String, Object> event = buildProductEvent(product, "PRODUCT_UPDATED");
        kafkaTemplate.send(productUpdatedTopic, product.getId(), event);
        log.info("Published product updated event for product ID: {}", product.getId());
    }

    private Map<String, Object> buildProductEvent(Product product, String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("productId", product.getId());
        event.put("sku", product.getSku());
        event.put("name", product.getName());
        event.put("price", product.getPrice());
        event.put("currency", product.getCurrency());
        event.put("categoryId", product.getCategoryId());
        event.put("sellerId", product.getSellerId());
        event.put("status", product.getStatus().name());
        event.put("timestamp", System.currentTimeMillis());
        return event;
    }
}
