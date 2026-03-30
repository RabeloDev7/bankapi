package com.leonardo.bankapi.service;

import com.leonardo.bankapi.entity.Account;
import com.leonardo.bankapi.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Camada de regras de negócio da conta
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Realiza depósito na conta
     */
    public Account deposit(String accountId, BigDecimal amount) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);

        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Conta não encontrada");
        }

        Account account = accountOpt.get();

        account.setBalance(account.getBalance().add(amount));

        return accountRepository.save(account);
    }

    /**
     * Realiza saque na conta
     */
    public Account withdraw(String accountId, BigDecimal amount) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);

        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Conta não encontrada");
        }

        Account account = accountOpt.get();

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Saldo insuficiente");
        }

        account.setBalance(account.getBalance().subtract(amount));

        return accountRepository.save(account);
    }
}