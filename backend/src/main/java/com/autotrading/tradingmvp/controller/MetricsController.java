package com.autotrading.tradingmvp.controller;

import com.autotrading.tradingmvp.dto.LatencyMetricResponse;
import com.autotrading.tradingmvp.service.LatencyMetricService;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final LatencyMetricService latencyMetricService;

    public MetricsController(LatencyMetricService latencyMetricService) {
        this.latencyMetricService = latencyMetricService;
    }

    @GetMapping("/latency")
    public ResponseEntity<List<LatencyMetricResponse>> queryLatency(
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        OffsetDateTime fromTs = from != null ? OffsetDateTime.parse(from) : null;
        OffsetDateTime toTs = to != null ? OffsetDateTime.parse(to) : null;
        return ResponseEntity.ok(latencyMetricService.query(stage, fromTs, toTs));
    }
}
