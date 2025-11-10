package com.autotrading.tradingmvp.service;

import com.autotrading.tradingmvp.domain.model.Account;
import com.autotrading.tradingmvp.domain.model.Instrument;
import com.autotrading.tradingmvp.domain.model.StrategyDefinition;
import com.autotrading.tradingmvp.domain.model.StrategyStatus;
import com.autotrading.tradingmvp.domain.repository.AccountRepository;
import com.autotrading.tradingmvp.domain.repository.InstrumentRepository;
import com.autotrading.tradingmvp.domain.repository.StrategyDefinitionRepository;
import com.autotrading.tradingmvp.dto.StrategyCreateRequest;
import com.autotrading.tradingmvp.dto.StrategyResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StrategyService {

    private static final Logger log = LoggerFactory.getLogger(StrategyService.class);

    private final StrategyDefinitionRepository strategyRepository;
    private final AccountRepository accountRepository;
    private final InstrumentRepository instrumentRepository;
    private final StrategyMapper mapper;

    public StrategyService(StrategyDefinitionRepository strategyRepository,
                           AccountRepository accountRepository,
                           InstrumentRepository instrumentRepository,
                           StrategyMapper mapper) {
        this.strategyRepository = strategyRepository;
        this.accountRepository = accountRepository;
        this.instrumentRepository = instrumentRepository;
        this.mapper = mapper;
    }

    @Transactional
    public StrategyResponse createStrategy(StrategyCreateRequest request) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.accountId()));
        Instrument instrument = instrumentRepository.findById(request.instrumentId())
                .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + request.instrumentId()));

        StrategyStatus status = request.status() != null ? request.status() : StrategyStatus.ACTIVE;
        String parameters = mapper.toJson(request.parameters());

        StrategyDefinition definition = new StrategyDefinition(
                request.name(),
                account,
                instrument,
                request.type(),
                request.cron(),
                status,
                request.startAt(),
                request.endAt(),
                parameters
        );
        definition.setDescription(request.description());

        StrategyDefinition saved = strategyRepository.save(definition);
        log.info("Created strategy id={} name={} type={}", saved.getId(), saved.getName(), saved.getType());
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StrategyResponse> listStrategies() {
        return strategyRepository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StrategyResponse getStrategy(Long strategyId) {
        StrategyDefinition definition = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found: " + strategyId));
        return mapper.toResponse(definition);
    }
}
