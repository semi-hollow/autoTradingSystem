package com.autotrading.tradingmvp.service.strategy;

import com.autotrading.tradingmvp.domain.model.Account;
import com.autotrading.tradingmvp.domain.model.Instrument;
import com.autotrading.tradingmvp.domain.model.StrategyDefinition;
import com.autotrading.tradingmvp.domain.model.StrategyStatus;
import com.autotrading.tradingmvp.domain.model.StrategyType;
import com.autotrading.tradingmvp.dto.OrderCreateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class GridBuyStrategyHandlerTest {

    private final GridBuyStrategyHandler handler = new GridBuyStrategyHandler();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void buildsLimitOrdersPerLevel() throws Exception {
        StrategyDefinition definition = buildStrategy();
        JsonNode parameters = objectMapper.readTree("""
            {
              "anchorPrice": 100,
              "stepPercent": 1.0,
              "cashPerLevel": 200,
              "levels": 2
            }
            """);

        List<OrderCreateRequest> orders = handler.buildOrders(definition, parameters);

        assertThat(orders).hasSize(2);
        OrderCreateRequest first = orders.get(0);
        assertThat(first.limitPrice()).isEqualByComparingTo(new BigDecimal("99.0000"));
        assertThat(first.qty()).isEqualByComparingTo(new BigDecimal("2.020202"));
        OrderCreateRequest second = orders.get(1);
        assertThat(second.limitPrice()).isEqualByComparingTo(new BigDecimal("98.0000"));
        assertThat(second.qty()).isEqualByComparingTo(new BigDecimal("2.040816"));
    }

    private StrategyDefinition buildStrategy() {
        Account account = new Account("Seed", "USD", BigDecimal.TEN);
        Instrument instrument = new Instrument("QQQ", "USD");
        StrategyDefinition definition = new StrategyDefinition(
                "Grid",
                account,
                instrument,
                StrategyType.GRID_BUY,
                "0 0 * * * *",
                StrategyStatus.ACTIVE,
                OffsetDateTime.now(),
                null,
                "{}"
        );
        ReflectionTestUtils.setField(account, "id", 1L);
        ReflectionTestUtils.setField(instrument, "id", 2L);
        ReflectionTestUtils.setField(definition, "id", 3L);
        return definition;
    }
}
