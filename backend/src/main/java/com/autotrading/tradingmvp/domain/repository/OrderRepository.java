package com.autotrading.tradingmvp.domain.repository;

import com.autotrading.tradingmvp.domain.model.Order;
import com.autotrading.tradingmvp.domain.model.OrderStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByClientOrderId(String clientOrderId);

    List<Order> findByAccountIdAndCreatedAtBetween(Long accountId, OffsetDateTime from, OffsetDateTime to);

    List<Order> findByStatus(OrderStatus status);
}
