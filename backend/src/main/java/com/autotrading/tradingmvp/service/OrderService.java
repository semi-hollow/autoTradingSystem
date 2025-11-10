package com.autotrading.tradingmvp.service;

import com.autotrading.tradingmvp.adapter.Ack;
import com.autotrading.tradingmvp.adapter.BrokerAdapter;
import com.autotrading.tradingmvp.adapter.PlaceOrderCommand;
import com.autotrading.tradingmvp.domain.model.Account;
import com.autotrading.tradingmvp.domain.model.Instrument;
import com.autotrading.tradingmvp.domain.model.Order;
import com.autotrading.tradingmvp.domain.model.OrderSide;
import com.autotrading.tradingmvp.domain.model.OrderStatus;
import com.autotrading.tradingmvp.domain.model.OrderType;
import com.autotrading.tradingmvp.domain.model.StrategyDefinition;
import com.autotrading.tradingmvp.domain.repository.AccountRepository;
import com.autotrading.tradingmvp.domain.repository.InstrumentRepository;
import com.autotrading.tradingmvp.domain.repository.OrderRepository;
import com.autotrading.tradingmvp.domain.repository.StrategyDefinitionRepository;
import com.autotrading.tradingmvp.dto.OrderCreateRequest;
import com.autotrading.tradingmvp.dto.OrderResponse;
import com.autotrading.tradingmvp.orderflow.OrderEventPublisher;
import com.autotrading.tradingmvp.orderflow.OrderSubmittedEvent;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final InstrumentRepository instrumentRepository;
    private final BrokerAdapter brokerAdapter;
    private final StringRedisTemplate redisTemplate;
    private final StrategyDefinitionRepository strategyRepository;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(OrderRepository orderRepository,
                        AccountRepository accountRepository,
                        InstrumentRepository instrumentRepository,
                        BrokerAdapter brokerAdapter,
                        StringRedisTemplate redisTemplate,
                        StrategyDefinitionRepository strategyRepository,
                        OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.accountRepository = accountRepository;
        this.instrumentRepository = instrumentRepository;
        this.brokerAdapter = brokerAdapter;
        this.redisTemplate = redisTemplate;
        this.strategyRepository = strategyRepository;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.accountId()));
        Instrument instrument = instrumentRepository.findById(request.instrumentId())
                .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + request.instrumentId()));

        StrategyDefinition strategy = resolveStrategy(request.strategyId());

        String clientOrderId = generateClientOrderId(request);
        ensureClientOrderIdIsUnique(clientOrderId);

        Order order = new Order(clientOrderId, account, instrument, request.side(), request.type());
        if (request.qty() != null) {
            order.setQty(request.qty());
        }
        if (request.cashAmount() != null) {
            order.setCashAmount(request.cashAmount());
        }
        if (request.limitPrice() != null) {
            order.setLimitPrice(request.limitPrice());
        }
        order.setStrategy(strategy);
        if (request.strategy() != null && !request.strategy().isBlank()) {
            order.setStrategyTag(request.strategy());
        } else if (strategy != null) {
            order.setStrategyTag(strategy.getName());
        }
        order.setStatus(OrderStatus.PENDING_SUBMIT);

        Order saved = orderRepository.save(order);

        Ack ack = brokerAdapter.placeOrder(
                new PlaceOrderCommand(
                        saved.getClientOrderId(),
                        saved.getInstrument().getId(),
                        saved.getSide(),
                        saved.getType(),
                        saved.getQty(),
                        saved.getCashAmount(),
                        saved.getLimitPrice()
                )
        );

        if (!ack.accepted()) {
            saved.setStatus(OrderStatus.REJECTED);
            saved.setReason(ack.message());
        } else {
            saved.setStatus(OrderStatus.SUBMITTED);
            publishSubmittedEvent(saved);
        }
        log.info("Order submitted clientOrderId={} status={}", saved.getClientOrderId(), saved.getStatus());
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (order.getStatus() == OrderStatus.FILLED || order.getStatus() == OrderStatus.CANCELED) {
            return toResponse(order);
        }
        brokerAdapter.cancelOrder(order.getClientOrderId());
        order.setStatus(OrderStatus.CANCELED);
        order.setReason("Canceled by user");
        log.info("Canceled order id={}", orderId);
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders(String status, Long accountId) {
        if (status != null && !status.isBlank()) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase(Locale.ROOT));
            return orderRepository.findByStatus(orderStatus).stream()
                    .filter(order -> accountId == null || order.getAccount().getId().equals(accountId))
                    .map(this::toResponse)
                    .toList();
        }
        if (accountId != null) {
            return orderRepository.findAll().stream()
                    .filter(order -> order.getAccount().getId().equals(accountId))
                    .map(this::toResponse)
                    .toList();
        }
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private void ensureClientOrderIdIsUnique(String clientOrderId) {
        String key = "trading:order:client:" + clientOrderId;
        Boolean set = redisTemplate.opsForValue()
                .setIfAbsent(key, OffsetDateTime.now().toString(), java.time.Duration.ofHours(12));
        if (Boolean.FALSE.equals(set)) {
            throw new IllegalStateException("Duplicate clientOrderId detected: " + clientOrderId);
        }
    }

    private String generateClientOrderId(OrderCreateRequest request) {
        if (request.strategyId() != null) {
            return "strategy-" + request.strategyId() + "-" + System.currentTimeMillis();
        }
        return UUID.randomUUID().toString();
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getClientOrderId(),
                order.getAccount().getId(),
                order.getInstrument().getId(),
                order.getStrategy() != null ? order.getStrategy().getId() : null,
                order.getStrategy() != null ? order.getStrategy().getName() : null,
                order.getStrategyTag(),
                order.getSide(),
                order.getType(),
                order.getQty(),
                order.getCashAmount(),
                order.getLimitPrice(),
                order.getStatus(),
                order.getReason(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private StrategyDefinition resolveStrategy(Long strategyId) {
        if (strategyId == null) {
            return null;
        }
        return strategyRepository.findById(strategyId)
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found: " + strategyId));
    }

    private void publishSubmittedEvent(Order order) {
        OrderSubmittedEvent event = new OrderSubmittedEvent(
                order.getId(),
                order.getClientOrderId(),
                order.getAccount().getId(),
                order.getInstrument().getId(),
                order.getSide().name(),
                order.getType().name(),
                order.getQty(),
                order.getCashAmount(),
                order.getLimitPrice(),
                OffsetDateTime.now()
        );
        orderEventPublisher.publish(event);
    }
}
