package com.autotrading.tradingmvp.dto;

import com.autotrading.tradingmvp.domain.model.OrderSide;
import com.autotrading.tradingmvp.domain.model.OrderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OrderCreateRequest(
        @NotNull Long accountId,
        @NotNull Long instrumentId,
        @NotNull OrderSide side,
        @NotNull OrderType type,
        @DecimalMin(value = "0.00") BigDecimal qty,
        @DecimalMin(value = "0.00") BigDecimal cashAmount,
        @DecimalMin(value = "0.00") BigDecimal limitPrice,
        String strategy,
        Long strategyId
) {
}
