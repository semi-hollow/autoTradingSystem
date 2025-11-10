package com.autotrading.tradingmvp.dto;

import com.autotrading.tradingmvp.domain.model.StrategyStatus;
import com.autotrading.tradingmvp.domain.model.StrategyType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record StrategyCreateRequest(
        @NotBlank String name,
        String description,
        @NotNull Long accountId,
        @NotNull Long instrumentId,
        @NotNull StrategyType type,
        @NotBlank String cron,
        StrategyStatus status,
        @NotNull OffsetDateTime startAt,
        OffsetDateTime endAt,
        @NotNull JsonNode parameters
) {
}
