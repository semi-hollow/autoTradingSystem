package com.autotrading.tradingmvp.controller;

import com.autotrading.tradingmvp.dto.PositionResponse;
import com.autotrading.tradingmvp.service.PositionService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    public ResponseEntity<List<PositionResponse>> listPositions(@RequestParam(required = false) Long accountId) {
        return ResponseEntity.ok(positionService.listPositions(accountId));
    }
}
