package com.autotrading.tradingmvp.orderflow;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderSubmittedEvent(
        Long orderId,
        String clientOrderId,
        Long accountId,
        Long instrumentId,
        String side,
        String type,
        BigDecimal qty,
        BigDecimal cashAmount,
        BigDecimal limitPrice,
        OffsetDateTime submittedAt
) {
}
