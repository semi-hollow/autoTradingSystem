package com.autotrading.tradingmvp.service;

import com.autotrading.tradingmvp.domain.model.Account;
import com.autotrading.tradingmvp.domain.model.DcaPlan;
import com.autotrading.tradingmvp.domain.model.HolidayPolicy;
import com.autotrading.tradingmvp.domain.model.Instrument;
import com.autotrading.tradingmvp.domain.model.PlanStatus;
import com.autotrading.tradingmvp.domain.repository.AccountRepository;
import com.autotrading.tradingmvp.domain.repository.DcaPlanRepository;
import com.autotrading.tradingmvp.domain.repository.InstrumentRepository;
import com.autotrading.tradingmvp.dto.PlanCreateRequest;
import com.autotrading.tradingmvp.dto.PlanResponse;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanService {

    private static final Logger log = LoggerFactory.getLogger(PlanService.class);

    private final DcaPlanRepository planRepository;
    private final AccountRepository accountRepository;
    private final InstrumentRepository instrumentRepository;

    public PlanService(DcaPlanRepository planRepository,
                       AccountRepository accountRepository,
                       InstrumentRepository instrumentRepository) {
        this.planRepository = planRepository;
        this.accountRepository = accountRepository;
        this.instrumentRepository = instrumentRepository;
    }

    @Transactional
    public PlanResponse createPlan(PlanCreateRequest request) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.accountId()));
        Instrument instrument = instrumentRepository.findById(request.instrumentId())
                .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + request.instrumentId()));

        PlanStatus status = request.status() != null ? request.status() : PlanStatus.ACTIVE;
        HolidayPolicy policy = request.holidayPolicy() != null ? request.holidayPolicy() : HolidayPolicy.NEXT_BUSINESS_DAY;

        DcaPlan plan = new DcaPlan(
                account,
                instrument,
                request.cashAmount(),
                request.cron(),
                request.startAt(),
                request.endAt(),
                status,
                policy
        );
        plan.setLastRunAt(null);
        DcaPlan saved = planRepository.save(plan);
        log.info("Created DCA plan id={} account={} instrument={}", saved.getId(), account.getId(), instrument.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> listPlans() {
        return planRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PlanResponse markRunNow(Long planId) {
        DcaPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        plan.setLastRunAt(OffsetDateTime.now());
        log.info("Marked plan id={} run-now at {}", planId, plan.getLastRunAt());
        return toResponse(plan);
    }

    private PlanResponse toResponse(DcaPlan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getAccount().getId(),
                plan.getInstrument().getId(),
                plan.getCashAmount(),
                plan.getCron(),
                plan.getStartAt(),
                plan.getEndAt(),
                plan.getStatus(),
                plan.getLastRunAt(),
                plan.getHolidayPolicy()
        );
    }
}
