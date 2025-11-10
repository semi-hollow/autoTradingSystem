package com.autotrading.tradingmvp.scheduler;

import com.autotrading.tradingmvp.domain.model.StrategyDefinition;
import com.autotrading.tradingmvp.domain.model.StrategyStatus;
import com.autotrading.tradingmvp.domain.model.StrategyTrigger;
import com.autotrading.tradingmvp.domain.repository.StrategyDefinitionRepository;
import com.autotrading.tradingmvp.service.StrategyExecutionService;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StrategyScheduler {

    private static final Logger log = LoggerFactory.getLogger(StrategyScheduler.class);

    private final StrategyDefinitionRepository strategyRepository;
    private final StrategyExecutionService executionService;
    private final StringRedisTemplate redisTemplate;

    public StrategyScheduler(StrategyDefinitionRepository strategyRepository,
                             StrategyExecutionService executionService,
                             StringRedisTemplate redisTemplate) {
        this.strategyRepository = strategyRepository;
        this.executionService = executionService;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void executeDueStrategies() {
        OffsetDateTime now = OffsetDateTime.now();
        List<StrategyDefinition> candidates = strategyRepository.findRunnableStrategies(StrategyStatus.ACTIVE, now);
        if (candidates.isEmpty()) {
            return;
        }
        candidates.forEach(strategy -> {
            try {
                if (!isDue(strategy, now)) {
                    return;
                }
                if (!acquireLock(strategy.getId())) {
                    return;
                }
                executionService.executeStrategy(strategy.getId(), StrategyTrigger.SCHEDULED);
            } catch (Exception ex) {
                log.error("Failed to execute strategy id={}", strategy.getId(), ex);
            }
        });
    }

    private boolean isDue(StrategyDefinition strategy, OffsetDateTime now) {
        CronExpression expression = CronExpression.parse(strategy.getCron());
        ZonedDateTime reference = strategy.getLastRunAt() != null
                ? strategy.getLastRunAt().atZoneSameInstant(ZoneId.systemDefault())
                : strategy.getStartAt().atZoneSameInstant(ZoneId.systemDefault());
        ZonedDateTime next = expression.next(reference);
        ZonedDateTime current = now.atZoneSameInstant(ZoneId.systemDefault());
        return next != null && !next.isAfter(current);
    }

    private boolean acquireLock(Long strategyId) {
        String key = "trading:strategy:lock:" + strategyId;
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, "locked", java.time.Duration.ofSeconds(55));
        return Boolean.TRUE.equals(acquired);
    }
}
