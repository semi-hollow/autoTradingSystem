package com.autotrading.tradingmvp.domain.repository;

import com.autotrading.tradingmvp.domain.model.Position;
import com.autotrading.tradingmvp.domain.model.id.PositionId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, PositionId> {

    List<Position> findByAccount_Id(Long accountId);
}
