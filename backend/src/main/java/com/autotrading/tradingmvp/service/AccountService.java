package com.autotrading.tradingmvp.service;

import com.autotrading.tradingmvp.domain.repository.AccountRepository;
import com.autotrading.tradingmvp.dto.AccountResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listAccounts() {
        return accountRepository.findAll().stream()
                .map(account -> new AccountResponse(
                        account.getId(),
                        account.getName(),
                        account.getBaseCurrency(),
                        account.getCashAvailable()
                )).toList();
    }
}
