package com.autotrading.tradingmvp.adapter;

import com.autotrading.tradingmvp.domain.model.OrderSide;
import com.autotrading.tradingmvp.domain.model.OrderType;
import java.math.BigDecimal;

public record PlaceOrderCommand(
        String clientOrderId,
        Long instrumentId,
        OrderSide side,
        OrderType type,
        BigDecimal qty,
        BigDecimal cashAmount,
        BigDecimal limitPrice
) {
}
