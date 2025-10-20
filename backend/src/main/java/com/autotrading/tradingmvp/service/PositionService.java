package com.autotrading.tradingmvp.service;

import com.autotrading.tradingmvp.domain.model.Position;
import com.autotrading.tradingmvp.domain.repository.PositionRepository;
import com.autotrading.tradingmvp.dto.PositionResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PositionService {

    private final PositionRepository positionRepository;

    public PositionService(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    @Transactional(readOnly = true)
    public List<PositionResponse> listPositions(Long accountId) {
        List<Position> positions = accountId != null
                ? positionRepository.findByAccount_Id(accountId)
                : positionRepository.findAll();

        return positions.stream()
                .map(position -> new PositionResponse(
                        position.getAccount().getId(),
                        position.getInstrument().getId(),
                        position.getQty(),
                        position.getAvgPrice(),
                        position.getUpdatedAt()
                ))
                .toList();
    }
}
