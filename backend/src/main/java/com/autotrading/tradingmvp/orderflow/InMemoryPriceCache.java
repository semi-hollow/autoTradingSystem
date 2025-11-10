package com.autotrading.tradingmvp.orderflow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class InMemoryPriceCache {

    private final ConcurrentHashMap<Long, BigDecimal> priceStore = new ConcurrentHashMap<>();

    public BigDecimal nextPrice(Long instrumentId) {
        return priceStore.compute(instrumentId, (id, previous) -> {
            BigDecimal base = previous != null ? previous : BigDecimal.valueOf(100 + id);
            double drift = ThreadLocalRandom.current().nextDouble(-1.5, 1.5);
            BigDecimal updated = base.add(BigDecimal.valueOf(drift));
            if (updated.signum() <= 0) {
                updated = BigDecimal.valueOf(1);
            }
            return updated.setScale(4, RoundingMode.HALF_UP);
        });
    }
}
