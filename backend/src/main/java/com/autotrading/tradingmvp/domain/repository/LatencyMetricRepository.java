package com.autotrading.tradingmvp.domain.repository;

import com.autotrading.tradingmvp.domain.model.LatencyMetric;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LatencyMetricRepository extends JpaRepository<LatencyMetric, Long> {

    List<LatencyMetric> findByStageAndTimestampBetween(String stage, OffsetDateTime from, OffsetDateTime to);
}
