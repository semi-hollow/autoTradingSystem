package com.autotrading.tradingmvp.controller;

import com.autotrading.tradingmvp.dto.ExecutionResponse;
import com.autotrading.tradingmvp.service.ExecutionService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/executions")
public class ExecutionController {

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @GetMapping
    public ResponseEntity<List<ExecutionResponse>> findByOrder(@RequestParam Long orderId) {
        return ResponseEntity.ok(executionService.findByOrderId(orderId));
    }
}
