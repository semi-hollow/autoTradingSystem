package com.autotrading.tradingmvp.dto;

import com.autotrading.tradingmvp.domain.model.HolidayPolicy;
import com.autotrading.tradingmvp.domain.model.PlanStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PlanResponse(
        Long id,
        Long accountId,
        Long instrumentId,
        BigDecimal cashAmount,
        String cron,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        PlanStatus status,
        OffsetDateTime lastRunAt,
        HolidayPolicy holidayPolicy
) {
}
