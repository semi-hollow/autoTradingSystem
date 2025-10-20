package com.autotrading.tradingmvp.adapter;

public record Ack(
        String clientOrderId,
        boolean accepted,
        String message
) {
}
