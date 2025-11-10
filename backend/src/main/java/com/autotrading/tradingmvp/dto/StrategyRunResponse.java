package com.autotrading.tradingmvp.dto;

import com.autotrading.tradingmvp.domain.model.StrategyTrigger;
import java.util.List;

public record StrategyRunResponse(
        StrategyResponse strategy,
        StrategyTrigger trigger,
        List<OrderResponse> orders
) {
}
