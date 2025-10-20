package com.autotrading.tradingmvp;

import com.autotrading.tradingmvp.adapter.config.StubBrokerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(StubBrokerProperties.class)
@EnableScheduling
public class TradingMvpApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradingMvpApplication.class, args);
    }
}
