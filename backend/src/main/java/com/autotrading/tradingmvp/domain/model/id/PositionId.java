package com.autotrading.tradingmvp.domain.model.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PositionId implements Serializable {

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "instrument_id")
    private Long instrumentId;

    protected PositionId() {
        // for JPA
    }

    public PositionId(Long accountId, Long instrumentId) {
        this.accountId = accountId;
        this.instrumentId = instrumentId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PositionId that = (PositionId) o;
        return Objects.equals(accountId, that.accountId) && Objects.equals(instrumentId, that.instrumentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, instrumentId);
    }
}
