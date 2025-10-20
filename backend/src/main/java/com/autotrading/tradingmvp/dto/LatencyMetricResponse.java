package com.autotrading.tradingmvp.dto;

import java.time.OffsetDateTime;

public record LatencyMetricResponse(
        String stage,
        long latencyMs,
        OffsetDateTime timestamp
) {
}
