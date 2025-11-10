package com.autotrading.tradingmvp.domain.repository;

import com.autotrading.tradingmvp.domain.model.StrategyDefinition;
import com.autotrading.tradingmvp.domain.model.StrategyStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StrategyDefinitionRepository extends JpaRepository<StrategyDefinition, Long> {

    Optional<StrategyDefinition> findByName(String name);

    @Query("""
        select s
        from StrategyDefinition s
        where s.status = :status
          and s.startAt <= :now
          and (s.endAt is null or s.endAt >= :now)
    """)
    List<StrategyDefinition> findRunnableStrategies(@Param("status") StrategyStatus status,
                                                    @Param("now") OffsetDateTime now);
}
