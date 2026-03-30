package com.leonardo.bankapi.controller;

import com.leonardo.bankapi.entity.Account;
import com.leonardo.bankapi.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Controlador responsável pelas operações da conta
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Endpoint para depósito
     */
    @PostMapping("/{id}/deposit")
    public Account deposit(@PathVariable String id, @RequestParam BigDecimal amount) {
        return accountService.deposit(id, amount);
    }

    /**
     * Endpoint para saque
     */
    @PostMapping("/{id}/withdraw")
    public Account withdraw(@PathVariable String id, @RequestParam BigDecimal amount) {
        return accountService.withdraw(id, amount);
    }
}
