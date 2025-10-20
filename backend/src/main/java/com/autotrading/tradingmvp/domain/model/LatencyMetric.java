package com.autotrading.tradingmvp.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "metrics_latency")
public class LatencyMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String stage;

    @Column(name = "latency_ms", nullable = false)
    private long latencyMs;

    @Column(name = "ts", nullable = false)
    private OffsetDateTime timestamp = OffsetDateTime.now();

    protected LatencyMetric() {
        // for JPA
    }

    public LatencyMetric(String stage, long latencyMs, OffsetDateTime timestamp) {
        this.stage = stage;
        this.latencyMs = latencyMs;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
