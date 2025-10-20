package com.autotrading.tradingmvp.dto;

import com.autotrading.tradingmvp.domain.model.HolidayPolicy;
import com.autotrading.tradingmvp.domain.model.PlanStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PlanCreateRequest(
        @NotNull Long accountId,
        @NotNull Long instrumentId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal cashAmount,
        @NotBlank @Size(max = 64) String cron,
        @NotNull OffsetDateTime startAt,
        OffsetDateTime endAt,
        HolidayPolicy holidayPolicy,
        PlanStatus status
) {
}
