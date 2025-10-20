package com.autotrading.tradingmvp.controller;

import com.autotrading.tradingmvp.dto.PlanCreateRequest;
import com.autotrading.tradingmvp.dto.PlanResponse;
import com.autotrading.tradingmvp.service.PlanService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    public ResponseEntity<PlanResponse> createPlan(@Valid @RequestBody PlanCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.createPlan(request));
    }

    @GetMapping
    public ResponseEntity<List<PlanResponse>> listPlans() {
        return ResponseEntity.ok(planService.listPlans());
    }

    @PostMapping("/{planId}/run-now")
    public ResponseEntity<PlanResponse> runPlanNow(@PathVariable Long planId) {
        return ResponseEntity.ok(planService.markRunNow(planId));
    }
}
