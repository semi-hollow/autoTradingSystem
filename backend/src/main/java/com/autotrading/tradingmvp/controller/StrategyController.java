package com.autotrading.tradingmvp.controller;

import com.autotrading.tradingmvp.domain.model.StrategyTrigger;
import com.autotrading.tradingmvp.dto.StrategyCreateRequest;
import com.autotrading.tradingmvp.dto.StrategyResponse;
import com.autotrading.tradingmvp.dto.StrategyRunResponse;
import com.autotrading.tradingmvp.service.StrategyExecutionService;
import com.autotrading.tradingmvp.service.StrategyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/strategies")
public class StrategyController {

    private final StrategyService strategyService;
    private final StrategyExecutionService executionService;

    public StrategyController(StrategyService strategyService,
                              StrategyExecutionService executionService) {
        this.strategyService = strategyService;
        this.executionService = executionService;
    }

    @PostMapping
    public ResponseEntity<StrategyResponse> createStrategy(@Valid @RequestBody StrategyCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(strategyService.createStrategy(request));
    }

    @GetMapping
    public ResponseEntity<List<StrategyResponse>> listStrategies() {
        return ResponseEntity.ok(strategyService.listStrategies());
    }

    @PostMapping("/{strategyId}/run-now")
    public ResponseEntity<StrategyRunResponse> runStrategy(@PathVariable Long strategyId) {
        StrategyRunResponse response = executionService.executeStrategy(strategyId, StrategyTrigger.MANUAL);
        return ResponseEntity.ok(response);
    }
}
