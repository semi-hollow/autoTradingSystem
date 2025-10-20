package com.autotrading.tradingmvp.service;

import com.autotrading.tradingmvp.domain.repository.ExecutionRepository;
import com.autotrading.tradingmvp.dto.ExecutionResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExecutionService {

    private final ExecutionRepository executionRepository;

    public ExecutionService(ExecutionRepository executionRepository) {
        this.executionRepository = executionRepository;
    }

    @Transactional(readOnly = true)
    public List<ExecutionResponse> findByOrderId(Long orderId) {
        return executionRepository.findByOrderId(orderId).stream()
                .map(execution -> new ExecutionResponse(
                        execution.getId(),
                        execution.getOrder().getId(),
                        execution.getPrice(),
                        execution.getQty(),
                        execution.getFee(),
                        execution.getTimestamp()
                ))
                .toList();
    }
}
