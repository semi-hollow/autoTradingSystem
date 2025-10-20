package com.autotrading.tradingmvp.dto;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String name,
        String baseCurrency,
        BigDecimal cashAvailable
) {
}
