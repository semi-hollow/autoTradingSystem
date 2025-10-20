package com.autotrading.tradingmvp.domain.repository;

import com.autotrading.tradingmvp.domain.model.Execution;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionRepository extends JpaRepository<Execution, Long> {

    List<Execution> findByOrderId(Long orderId);
}
