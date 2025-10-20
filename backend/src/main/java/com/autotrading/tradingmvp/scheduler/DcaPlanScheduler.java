package com.autotrading.tradingmvp.scheduler;

import com.autotrading.tradingmvp.domain.model.DcaPlan;
import com.autotrading.tradingmvp.domain.model.OrderSide;
import com.autotrading.tradingmvp.domain.model.OrderType;
import com.autotrading.tradingmvp.domain.model.PlanStatus;
import com.autotrading.tradingmvp.domain.repository.DcaPlanRepository;
import com.autotrading.tradingmvp.dto.OrderCreateRequest;
import com.autotrading.tradingmvp.service.OrderService;
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
public class DcaPlanScheduler {

    private static final Logger log = LoggerFactory.getLogger(DcaPlanScheduler.class);

    private final DcaPlanRepository planRepository;
    private final OrderService orderService;
    private final StringRedisTemplate redisTemplate;

    public DcaPlanScheduler(DcaPlanRepository planRepository,
                            OrderService orderService,
                            StringRedisTemplate redisTemplate) {
        this.planRepository = planRepository;
        this.orderService = orderService;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void executeDuePlans() {
        OffsetDateTime now = OffsetDateTime.now();
        List<DcaPlan> plans = planRepository.findActivePlans(PlanStatus.ACTIVE, now);
        if (plans.isEmpty()) {
            return;
        }
        plans.forEach(plan -> {
            try {
                if (!isPlanDue(plan, now)) {
                    return;
                }
                if (!acquireLock(plan.getId())) {
                    return;
                }
                orderService.createOrder(
                        new OrderCreateRequest(
                                plan.getId(),
                                plan.getAccount().getId(),
                                plan.getInstrument().getId(),
                                OrderSide.BUY,
                                OrderType.MARKET,
                                null,
                                plan.getCashAmount(),
                                null,
                                "DCA"
                        )
                );
                plan.setLastRunAt(now);
                log.info("Executed DCA plan id={} at {}", plan.getId(), now);
            } catch (Exception ex) {
                log.error("Failed to execute DCA plan id={}", plan.getId(), ex);
            }
        });
    }

    private boolean isPlanDue(DcaPlan plan, OffsetDateTime now) {
        CronExpression expression = CronExpression.parse(plan.getCron());
        ZonedDateTime last = plan.getLastRunAt() != null
                ? plan.getLastRunAt().atZoneSameInstant(ZoneId.systemDefault())
                : plan.getStartAt().atZoneSameInstant(ZoneId.systemDefault());
        ZonedDateTime next = expression.next(last);
        ZonedDateTime nowZoned = now.atZoneSameInstant(ZoneId.systemDefault());
        return next != null && !next.isAfter(nowZoned);
    }

    private boolean acquireLock(Long planId) {
        String key = "trading:dca:lock:" + planId;
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, "locked", java.time.Duration.ofSeconds(50));
        return Boolean.TRUE.equals(acquired);
    }
}
