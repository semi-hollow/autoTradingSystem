package com.autotrading.tradingmvp.service;

import com.autotrading.tradingmvp.domain.model.LatencyMetric;
import com.autotrading.tradingmvp.domain.repository.LatencyMetricRepository;
import com.autotrading.tradingmvp.dto.LatencyMetricResponse;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LatencyMetricService {

    private final LatencyMetricRepository latencyMetricRepository;

    public LatencyMetricService(LatencyMetricRepository latencyMetricRepository) {
        this.latencyMetricRepository = latencyMetricRepository;
    }

    @Transactional(readOnly = true)
    public List<LatencyMetricResponse> query(String stage, OffsetDateTime from, OffsetDateTime to) {
        List<LatencyMetric> metrics;
        if (stage != null && from != null && to != null) {
            metrics = latencyMetricRepository.findByStageAndTimestampBetween(stage, from, to);
        } else if (stage != null) {
            metrics = latencyMetricRepository.findAll().stream()
                    .filter(metric -> stage.equals(metric.getStage()))
                    .toList();
        } else {
            metrics = latencyMetricRepository.findAll();
        }

        return metrics.stream()
                .map(metric -> new LatencyMetricResponse(
                        metric.getStage(),
                        metric.getLatencyMs(),
                        metric.getTimestamp()
                ))
                .toList();
    }
}
