package com.autotrading.tradingmvp.domain.repository;

import com.autotrading.tradingmvp.domain.model.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByName(String name);
}
