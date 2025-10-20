package com.autotrading.tradingmvp.domain.repository;

import com.autotrading.tradingmvp.domain.model.DcaPlan;
import com.autotrading.tradingmvp.domain.model.PlanStatus;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DcaPlanRepository extends JpaRepository<DcaPlan, Long> {

    @Query("""
        select p
        from DcaPlan p
        where p.status = :status
          and p.startAt <= :now
          and (p.endAt is null or p.endAt >= :now)
    """)
    List<DcaPlan> findActivePlans( @Param("status") PlanStatus status, @Param("now") OffsetDateTime now);
}
