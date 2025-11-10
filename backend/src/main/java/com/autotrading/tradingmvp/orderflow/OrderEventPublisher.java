package com.autotrading.tradingmvp.orderflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);
    public static final String TOPIC = "trading.orders.submitted";

    private final KafkaTemplate<String, OrderSubmittedEvent> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, OrderSubmittedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(OrderSubmittedEvent event) {
        kafkaTemplate.send(TOPIC, event.clientOrderId(), event)
                .whenComplete((metadata, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish order event {}", event.clientOrderId(), ex);
                    } else {
                        log.debug("Published order event {} to {}-{}", event.clientOrderId(),
                                metadata.topic(), metadata.partition());
                    }
                });
    }
}
