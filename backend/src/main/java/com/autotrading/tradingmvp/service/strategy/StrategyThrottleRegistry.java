package com.autotrading.tradingmvp.service.strategy;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StrategyThrottleRegistry {

    private static final Logger log = LoggerFactory.getLogger(StrategyThrottleRegistry.class);

    private final ConcurrentHashMap<Long, Long> lastExecution = new ConcurrentHashMap<>();
    private final long windowMillis;

    public StrategyThrottleRegistry(@Value("${trading.strategy.min-window-seconds:30}") long minWindowSeconds) {
        this.windowMillis = Duration.ofSeconds(minWindowSeconds).toMillis();
    }

    public boolean tryAcquire(Long strategyId) {
        if (strategyId == null) {
            return true;
        }
        long now = System.currentTimeMillis();
        AtomicBoolean acquired = new AtomicBoolean(false);
        lastExecution.compute(strategyId, (id, previous) -> {
            if (previous == null || now - previous >= windowMillis) {
                acquired.set(true);
                return now;
            }
            return previous;
        });
        if (!acquired.get()) {
            log.debug("Strategy {} throttled (window {} ms)", strategyId, windowMillis);
        }
        return acquired.get();
    }
}
