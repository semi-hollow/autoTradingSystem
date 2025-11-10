package com.autotrading.tradingmvp.service;

import com.autotrading.tradingmvp.domain.model.StrategyDefinition;
import com.autotrading.tradingmvp.domain.model.StrategyTrigger;
import com.autotrading.tradingmvp.domain.repository.StrategyDefinitionRepository;
import com.autotrading.tradingmvp.dto.OrderCreateRequest;
import com.autotrading.tradingmvp.dto.OrderResponse;
import com.autotrading.tradingmvp.dto.StrategyRunResponse;
import com.autotrading.tradingmvp.dto.StrategyResponse;
import com.autotrading.tradingmvp.service.strategy.StrategyHandler;
import com.autotrading.tradingmvp.service.strategy.StrategyThrottleRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StrategyExecutionService {

    private static final Logger log = LoggerFactory.getLogger(StrategyExecutionService.class);

    private final StrategyDefinitionRepository strategyRepository;
    private final OrderService orderService;
    private final StrategyMapper mapper;
    private final Map<String, StrategyHandler> handlerRegistry;
    private final StrategyThrottleRegistry throttleRegistry;

    public StrategyExecutionService(StrategyDefinitionRepository strategyRepository,
                                    OrderService orderService,
                                    StrategyMapper mapper,
                                    StrategyThrottleRegistry throttleRegistry,
                                    List<StrategyHandler> handlers) {
        this.strategyRepository = strategyRepository;
        this.orderService = orderService;
        this.mapper = mapper;
        this.throttleRegistry = throttleRegistry;
        this.handlerRegistry = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(handler -> handler.type().name(), Function.identity()));
    }

    @Transactional
    public StrategyRunResponse executeStrategy(Long strategyId, StrategyTrigger trigger) {
        StrategyDefinition definition = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found: " + strategyId));
        return execute(definition, trigger);
    }

    @Transactional
    public StrategyRunResponse execute(StrategyDefinition definition, StrategyTrigger trigger) {
        StrategyHandler handler = handlerRegistry.get(definition.getType().name());
        if (handler == null) {
            throw new IllegalStateException("No handler registered for type " + definition.getType());
        }
        if (!throttleRegistry.tryAcquire(definition.getId())) {
            log.warn("Strategy {} hit throttle window, skipping execution", definition.getId());
            StrategyResponse response = mapper.toResponse(definition);
            return new StrategyRunResponse(response, trigger, List.of());
        }
        JsonNode parameters = mapper.toNode(definition.getParametersJson());
        List<OrderCreateRequest> requests = handler.buildOrders(definition, parameters);
        List<OrderResponse> orders = requests.stream()
                .map(orderService::createOrder)
                .toList();
        definition.setLastRunAt(OffsetDateTime.now());
        StrategyDefinition saved = strategyRepository.save(definition);
        StrategyResponse response = mapper.toResponse(saved);
        log.info("Executed strategy id={} type={} trigger={} orders={}",
                saved.getId(), saved.getType(), trigger, orders.size());
        return new StrategyRunResponse(response, trigger, orders);
    }
}
