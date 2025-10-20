package com.autotrading.tradingmvp.adapter;

import com.autotrading.tradingmvp.adapter.config.StubBrokerProperties;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StubBrokerAdapter implements BrokerAdapter {

    private static final Logger log = LoggerFactory.getLogger(StubBrokerAdapter.class);

    private final StubBrokerProperties properties;

    public StubBrokerAdapter(StubBrokerProperties properties) {
        this.properties = properties;
    }

    @Override
    public Ack placeOrder(PlaceOrderCommand command) {
        simulateLatency();
        double decision = ThreadLocalRandom.current().nextDouble();
        if (decision < properties.getRejectRate()) {
            log.warn("Stub adapter rejected order clientOrderId={}", command.clientOrderId());
            return new Ack(command.clientOrderId(), false, "Rejected by stub adapter");
        }
        log.debug("Stub adapter accepted order clientOrderId={}", command.clientOrderId());
        return new Ack(command.clientOrderId(), true, "Accepted");
    }

    @Override
    public void cancelOrder(String clientOrderId) {
        simulateLatency();
        log.info("Stub adapter acknowledged cancel for clientOrderId={}", clientOrderId);
    }

    private void simulateLatency() {
        Duration latency = properties.getP50Latency();
        if (latency.isZero()) {
            return;
        }
        try {
            Thread.sleep(Math.max(10L, latency.toMillis()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
