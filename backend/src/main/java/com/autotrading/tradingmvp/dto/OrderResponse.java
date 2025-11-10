package com.autotrading.tradingmvp.dto;

import com.autotrading.tradingmvp.domain.model.OrderSide;
import com.autotrading.tradingmvp.domain.model.OrderStatus;
import com.autotrading.tradingmvp.domain.model.OrderType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderResponse(
        Long id,
        String clientOrderId,
        Long accountId,
        Long instrumentId,
        Long strategyId,
        String strategyName,
        String strategyTag,
        OrderSide side,
        OrderType type,
        BigDecimal qty,
        BigDecimal cashAmount,
        BigDecimal limitPrice,
        OrderStatus status,
        String reason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
