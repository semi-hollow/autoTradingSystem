package com.autotrading.tradingmvp.config;

import com.autotrading.tradingmvp.domain.model.Account;
import com.autotrading.tradingmvp.domain.model.Instrument;
import com.autotrading.tradingmvp.domain.repository.AccountRepository;
import com.autotrading.tradingmvp.domain.repository.InstrumentRepository;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AccountRepository accountRepository;
    private final InstrumentRepository instrumentRepository;

    public DataInitializer(AccountRepository accountRepository,
                           InstrumentRepository instrumentRepository) {
        this.accountRepository = accountRepository;
        this.instrumentRepository = instrumentRepository;
    }

    @Override
    public void run(String... args) {
        accountRepository.findByName("Demo Account")
                .orElseGet(() -> {
                    Account account = new Account("Demo Account", "USD", new BigDecimal("100000.00"));
                    Account saved = accountRepository.save(account);
                    log.info("Seeded demo account id={}", saved.getId());
                    return saved;
                });

        ensureInstrumentExists("VOO", "USD");
        ensureInstrumentExists("QQQ", "USD");
    }

    private void ensureInstrumentExists(String symbol, String currency) {
        instrumentRepository.findBySymbol(symbol).orElseGet(() -> {
            Instrument instrument = new Instrument(symbol, currency);
            Instrument saved = instrumentRepository.save(instrument);
            log.info("Seeded instrument symbol={} id={}", symbol, saved.getId());
            return saved;
        });
    }
}
