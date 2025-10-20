package com.autotrading.tradingmvp.adapter.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "trading.stub")
public class StubBrokerProperties {

    private Duration p50Latency = Duration.ofMillis(150);
    private double partialFillRate = 0.1d;
    private double rejectRate = 0.05d;

    public Duration getP50Latency() {
        return p50Latency;
    }

    public void setP50Latency(Duration p50Latency) {
        this.p50Latency = p50Latency;
    }

    public double getPartialFillRate() {
        return partialFillRate;
    }

    public void setPartialFillRate(double partialFillRate) {
        this.partialFillRate = partialFillRate;
    }

    public double getRejectRate() {
        return rejectRate;
    }

    public void setRejectRate(double rejectRate) {
        this.rejectRate = rejectRate;
    }
}
