package com.autotrading.tradingmvp.adapter;

public interface BrokerAdapter {

    Ack placeOrder(PlaceOrderCommand command);

    void cancelOrder(String clientOrderId);
}
