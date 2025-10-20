package com.autotrading.tradingmvp.dto;

public record InstrumentResponse(
        Long id,
        String symbol,
        String currency
) {
}
