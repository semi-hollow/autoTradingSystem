package com.autotrading.tradingmvp.dto;

import com.autotrading.tradingmvp.domain.model.StrategyStatus;
import com.autotrading.tradingmvp.domain.model.StrategyType;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;

public record StrategyResponse(
        Long id,
        String name,
        String description,
        Long accountId,
        Long instrumentId,
        StrategyType type,
        StrategyStatus status,
        String cron,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        OffsetDateTime lastRunAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        JsonNode parameters
) {
}
