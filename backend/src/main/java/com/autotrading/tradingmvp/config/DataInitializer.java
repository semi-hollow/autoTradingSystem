package com.autotrading.tradingmvp.config;

import com.autotrading.tradingmvp.domain.model.Account;
import com.autotrading.tradingmvp.domain.model.Instrument;
import com.autotrading.tradingmvp.domain.model.StrategyDefinition;
import com.autotrading.tradingmvp.domain.model.StrategyStatus;
import com.autotrading.tradingmvp.domain.model.StrategyType;
import com.autotrading.tradingmvp.domain.repository.AccountRepository;
import com.autotrading.tradingmvp.domain.repository.InstrumentRepository;
import com.autotrading.tradingmvp.domain.repository.StrategyDefinitionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AccountRepository accountRepository;
    private final InstrumentRepository instrumentRepository;
    private final StrategyDefinitionRepository strategyRepository;
    private final ObjectMapper objectMapper;

    public DataInitializer(AccountRepository accountRepository,
                           InstrumentRepository instrumentRepository,
                           StrategyDefinitionRepository strategyRepository,
                           ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.instrumentRepository = instrumentRepository;
        this.strategyRepository = strategyRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) {
        Account demoAccount = accountRepository.findByName("Demo Account")
                .orElseGet(() -> {
                    Account account = new Account("Demo Account", "USD", new BigDecimal("100000.00"));
                    Account saved = accountRepository.save(account);
                    log.info("Seeded demo account id={}", saved.getId());
                    return saved;
                });

        Instrument voo = ensureInstrumentExists("VOO", "USD");
        Instrument qqq = ensureInstrumentExists("QQQ", "USD");

        seedStrategies(demoAccount, voo, qqq);
    }

    private Instrument ensureInstrumentExists(String symbol, String currency) {
        return instrumentRepository.findBySymbol(symbol).orElseGet(() -> {
            Instrument instrument = new Instrument(symbol, currency);
            Instrument saved = instrumentRepository.save(instrument);
            log.info("Seeded instrument symbol={} id={}", symbol, saved.getId());
            return saved;
        });
    }

    private void seedStrategies(Account account, Instrument voo, Instrument qqq) {
        strategyRepository.findByName("VOO Core DCA")
                .orElseGet(() -> createStrategy(
                        "VOO Core DCA",
                        "Automated fixed-amount DCA into core ETF position",
                        account,
                        voo,
                        StrategyType.FIXED_AMOUNT_DCA,
                        "0 0 9 * * MON-FRI",
                        OffsetDateTime.now().minusDays(1),
                        null,
                        objectMapper.createObjectNode().put("cashAmount", 500)
                ));

        strategyRepository.findByName("QQQ Grid Buy")
                .orElseGet(() -> createStrategy(
                        "QQQ Grid Buy",
                        "3-level buydown grid tuned for intraday dips",
                        account,
                        qqq,
                        StrategyType.GRID_BUY,
                        "0 */30 13-20 * * MON-FRI",
                        OffsetDateTime.now().minusDays(1),
                        null,
                        objectMapper.createObjectNode()
                                .put("anchorPrice", 400)
                                .put("stepPercent", 1.5)
                                .put("cashPerLevel", 250)
                                .put("levels", 3)
                ));
    }

    private StrategyDefinition createStrategy(String name,
                                              String description,
                                              Account account,
                                              Instrument instrument,
                                              StrategyType type,
                                              String cron,
                                              OffsetDateTime startAt,
                                              OffsetDateTime endAt,
                                              ObjectNode parameters) {
        StrategyDefinition definition = new StrategyDefinition(
                name,
                account,
                instrument,
                type,
                cron,
                StrategyStatus.ACTIVE,
                startAt,
                endAt,
                toJson(parameters)
        );
        definition.setDescription(description);
        StrategyDefinition saved = strategyRepository.save(definition);
        log.info("Seeded strategy {} id={}", name, saved.getId());
        return saved;
    }

    private String toJson(ObjectNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize seed strategy config", e);
        }
    }
}
