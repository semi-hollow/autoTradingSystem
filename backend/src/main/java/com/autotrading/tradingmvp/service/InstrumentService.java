package com.autotrading.tradingmvp.service;

import com.autotrading.tradingmvp.domain.repository.InstrumentRepository;
import com.autotrading.tradingmvp.dto.InstrumentResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;

    public InstrumentService(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    @Transactional(readOnly = true)
    public List<InstrumentResponse> listInstruments() {
        return instrumentRepository.findAll().stream()
                .map(instrument -> new InstrumentResponse(
                        instrument.getId(),
                        instrument.getSymbol(),
                        instrument.getCurrency()
                )).toList();
    }
}
