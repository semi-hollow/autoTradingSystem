package com.autotrading.tradingmvp.orderflow;

import com.autotrading.tradingmvp.domain.model.Account;
import com.autotrading.tradingmvp.domain.model.Execution;
import com.autotrading.tradingmvp.domain.model.Order;
import com.autotrading.tradingmvp.domain.model.OrderSide;
import com.autotrading.tradingmvp.domain.model.OrderStatus;
import com.autotrading.tradingmvp.domain.model.Position;
import com.autotrading.tradingmvp.domain.model.id.PositionId;
import com.autotrading.tradingmvp.domain.repository.ExecutionRepository;
import com.autotrading.tradingmvp.domain.repository.OrderRepository;
import com.autotrading.tradingmvp.domain.repository.PositionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderFillProcessor {

    private static final Logger log = LoggerFactory.getLogger(OrderFillProcessor.class);

    private final OrderRepository orderRepository;
    private final ExecutionRepository executionRepository;
    private final PositionRepository positionRepository;

    public OrderFillProcessor(OrderRepository orderRepository,
                              ExecutionRepository executionRepository,
                              PositionRepository positionRepository) {
        this.orderRepository = orderRepository;
        this.executionRepository = executionRepository;
        this.positionRepository = positionRepository;
    }

    @Transactional
    public void processFill(Long orderId, BigDecimal price, BigDecimal qty) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (order.getStatus() == OrderStatus.FILLED || order.getStatus() == OrderStatus.CANCELED) {
            log.debug("Skip fill for order {} status={}", orderId, order.getStatus());
            return;
        }

        BigDecimal fee = price.multiply(qty).multiply(new BigDecimal("0.0005"))
                .setScale(4, RoundingMode.HALF_UP);
        Execution execution = new Execution(order, price, qty, fee, OffsetDateTime.now());
        executionRepository.save(execution);

        adjustCash(order, price, qty, fee);
        upsertPosition(order, price, qty);

        order.setStatus(OrderStatus.FILLED);
        order.setReason("Filled via Kafka simulator");
        log.info("Order {} filled qty={} price={}", orderId, qty, price);
    }

    private void adjustCash(Order order, BigDecimal price, BigDecimal qty, BigDecimal fee) {
        Account account = order.getAccount();
        BigDecimal notional = price.multiply(qty).setScale(4, RoundingMode.HALF_UP);
        if (order.getSide() == OrderSide.BUY) {
            account.setCashAvailable(account.getCashAvailable().subtract(notional.add(fee)));
        } else {
            account.setCashAvailable(account.getCashAvailable().add(notional.subtract(fee)));
        }
    }

    private void upsertPosition(Order order, BigDecimal price, BigDecimal qty) {
        PositionId positionId = new PositionId(order.getAccount().getId(), order.getInstrument().getId());
        Position position = positionRepository.findById(positionId)
                .orElseGet(() -> new Position(order.getAccount(), order.getInstrument()));

        BigDecimal currentQty = position.getQty() != null ? position.getQty() : BigDecimal.ZERO;
        if (order.getSide() == OrderSide.BUY) {
            BigDecimal newQty = currentQty.add(qty);
            BigDecimal currentNotional = position.getAvgPrice().multiply(currentQty);
            BigDecimal newAvg = newQty.signum() == 0
                    ? BigDecimal.ZERO
                    : currentNotional.add(price.multiply(qty))
                    .divide(newQty, 4, RoundingMode.HALF_UP);
            position.setQty(newQty);
            position.setAvgPrice(newAvg);
        } else {
            BigDecimal newQty = currentQty.subtract(qty);
            if (newQty.signum() <= 0) {
                position.setQty(BigDecimal.ZERO);
                position.setAvgPrice(BigDecimal.ZERO);
            } else {
                position.setQty(newQty);
            }
        }
        position.setUpdatedAt(OffsetDateTime.now());
        positionRepository.save(position);
    }
}
