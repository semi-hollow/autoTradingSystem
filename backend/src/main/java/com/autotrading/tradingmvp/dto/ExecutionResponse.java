package com.autotrading.tradingmvp.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ExecutionResponse(
        Long id,
        Long orderId,
        BigDecimal price,
        BigDecimal qty,
        BigDecimal fee,
        OffsetDateTime timestamp
) {
}
