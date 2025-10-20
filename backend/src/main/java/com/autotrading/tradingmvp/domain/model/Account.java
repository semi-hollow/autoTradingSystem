package com.autotrading.tradingmvp.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String name;

    @Column(name = "base_currency", nullable = false, length = 12)
    private String baseCurrency;

    @Column(name = "cash_available", nullable = false, precision = 18, scale = 4)
    private BigDecimal cashAvailable = BigDecimal.ZERO;

    protected Account() {
        // for JPA
    }

    public Account(String name, String baseCurrency, BigDecimal cashAvailable) {
        this.name = name;
        this.baseCurrency = baseCurrency;
        this.cashAvailable = cashAvailable;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public BigDecimal getCashAvailable() {
        return cashAvailable;
    }

    public void setCashAvailable(BigDecimal cashAvailable) {
        this.cashAvailable = cashAvailable;
    }
}
