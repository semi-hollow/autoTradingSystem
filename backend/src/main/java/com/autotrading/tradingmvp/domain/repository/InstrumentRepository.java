package com.autotrading.tradingmvp.domain.repository;

import com.autotrading.tradingmvp.domain.model.Instrument;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    Optional<Instrument> findBySymbol(String symbol);
}
