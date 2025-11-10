package com.autotrading.tradingmvp.orderflow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final OrderFillProcessor orderFillProcessor;
    private final InMemoryPriceCache priceCache;

    public OrderEventListener(OrderFillProcessor orderFillProcessor,
                              InMemoryPriceCache priceCache) {
        this.orderFillProcessor = orderFillProcessor;
        this.priceCache = priceCache;
    }

    @KafkaListener(topics = OrderEventPublisher.TOPIC, groupId = "trading-mvp-matcher")
    public void onOrderSubmitted(OrderSubmittedEvent event) {
        try {
            BigDecimal price = priceCache.nextPrice(event.instrumentId());
            BigDecimal qty = resolveQuantity(event, price);
            orderFillProcessor.processFill(event.orderId(), price, qty);
        } catch (Exception ex) {
            log.error("Failed to process submitted order event {}", event.clientOrderId(), ex);
        }
    }

    private BigDecimal resolveQuantity(OrderSubmittedEvent event, BigDecimal price) {
        return Optional.ofNullable(event.qty())
                .filter(q -> q.signum() > 0)
                .orElseGet(() -> {
                    BigDecimal cash = Optional.ofNullable(event.cashAmount())
                            .filter(amount -> amount.signum() > 0)
                            .orElse(price.multiply(BigDecimal.valueOf(1)));
                    return cash.divide(price, 6, RoundingMode.HALF_UP);
                });
    }
}
