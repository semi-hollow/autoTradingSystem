package com.autotrading.tradingmvp.service.strategy;

import com.autotrading.tradingmvp.domain.model.StrategyDefinition;
import com.autotrading.tradingmvp.domain.model.StrategyType;
import com.autotrading.tradingmvp.domain.model.OrderSide;
import com.autotrading.tradingmvp.domain.model.OrderType;
import com.autotrading.tradingmvp.dto.OrderCreateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FixedAmountDcaHandler implements StrategyHandler {

    @Override
    public StrategyType type() {
        return StrategyType.FIXED_AMOUNT_DCA;
    }

    @Override
    public List<OrderCreateRequest> buildOrders(StrategyDefinition definition, JsonNode parameters) {
        BigDecimal cashAmount = getDecimal(parameters, "cashAmount");
        if (cashAmount == null || cashAmount.signum() <= 0) {
            throw new IllegalArgumentException("cashAmount must be positive for FIXED_AMOUNT_DCA");
        }
        OrderCreateRequest request = new OrderCreateRequest(
                null,
                definition.getAccount().getId(),
                definition.getInstrument().getId(),
                OrderSide.BUY,
                OrderType.MARKET,
                null,
                cashAmount,
                null,
                definition.getName(),
                definition.getId()
        );
        return List.of(request);
    }

    private BigDecimal getDecimal(JsonNode node, String field) {
        if (node == null || !node.has(field)) {
            return null;
        }
        return node.get(field).decimalValue();
    }
}
