package com.autotrading.tradingmvp.service.strategy;

import com.autotrading.tradingmvp.domain.model.StrategyDefinition;
import com.autotrading.tradingmvp.domain.model.StrategyType;
import com.autotrading.tradingmvp.dto.OrderCreateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public interface StrategyHandler {

    StrategyType type();

    List<OrderCreateRequest> buildOrders(StrategyDefinition definition, JsonNode parameters);
}
