package com.autotrading.tradingmvp.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PositionResponse(
        Long accountId,
        Long instrumentId,
        BigDecimal qty,
        BigDecimal avgPrice,
        OffsetDateTime updatedAt
) {
}
