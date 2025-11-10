package com.autotrading.tradingmvp.service.strategy;

import com.autotrading.tradingmvp.domain.model.OrderSide;
import com.autotrading.tradingmvp.domain.model.OrderType;
import com.autotrading.tradingmvp.domain.model.StrategyDefinition;
import com.autotrading.tradingmvp.domain.model.StrategyType;
import com.autotrading.tradingmvp.dto.OrderCreateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GridBuyStrategyHandler implements StrategyHandler {

    private static final MathContext MATH_CONTEXT = new MathContext(8, RoundingMode.HALF_UP);

    @Override
    public StrategyType type() {
        return StrategyType.GRID_BUY;
    }

    @Override
    public List<OrderCreateRequest> buildOrders(StrategyDefinition definition, JsonNode parameters) {
        BigDecimal anchorPrice = readDecimal(parameters, "anchorPrice");
        BigDecimal stepPercent = readDecimal(parameters, "stepPercent");
        BigDecimal cashPerLevel = readDecimal(parameters, "cashPerLevel");
        int levels = parameters.path("levels").asInt(0);

        if (anchorPrice == null || anchorPrice.signum() <= 0) {
            throw new IllegalArgumentException("anchorPrice must be provided for GRID_BUY");
        }
        if (cashPerLevel == null || cashPerLevel.signum() <= 0) {
            throw new IllegalArgumentException("cashPerLevel must be positive for GRID_BUY");
        }
        if (stepPercent == null || stepPercent.signum() <= 0) {
            throw new IllegalArgumentException("stepPercent must be positive");
        }
        if (levels <= 0) {
            throw new IllegalArgumentException("levels must be greater than zero");
        }

        BigDecimal stepFraction = stepPercent.divide(BigDecimal.valueOf(100), MATH_CONTEXT);
        List<OrderCreateRequest> requests = new ArrayList<>(levels);
        for (int level = 1; level <= levels; level++) {
            BigDecimal levelFraction = stepFraction.multiply(BigDecimal.valueOf(level), MATH_CONTEXT);
            BigDecimal reduction = BigDecimal.ONE.subtract(levelFraction, MATH_CONTEXT);
            BigDecimal limitPrice = anchorPrice.multiply(reduction, MATH_CONTEXT)
                    .setScale(4, RoundingMode.HALF_UP);
            if (limitPrice.signum() <= 0) {
                continue;
            }
            BigDecimal qty = cashPerLevel.divide(limitPrice, 6, RoundingMode.HALF_UP);
            OrderCreateRequest request = new OrderCreateRequest(
                    null,
                    definition.getAccount().getId(),
                    definition.getInstrument().getId(),
                    OrderSide.BUY,
                    OrderType.LIMIT,
                    qty,
                    null,
                    limitPrice,
                    definition.getName(),
                    definition.getId()
            );
            requests.add(request);
        }
        return requests;
    }

    private BigDecimal readDecimal(JsonNode node, String field) {
        if (node == null || !node.has(field)) {
            return null;
        }
        return node.get(field).decimalValue();
    }
}
