package com.autotrading.tradingmvp.controller;

import com.autotrading.tradingmvp.dto.InstrumentResponse;
import com.autotrading.tradingmvp.service.InstrumentService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @GetMapping
    public ResponseEntity<List<InstrumentResponse>> listInstruments() {
        return ResponseEntity.ok(instrumentService.listInstruments());
    }
}
