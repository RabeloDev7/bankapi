package com.leonardo.bankapi.controller;

import com.leonardo.bankapi.dto.*;
import com.leonardo.bankapi.service.AccountService;
import com.leonardo.bankapi.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@RequestParam Long userId) {
        return accountService.createAccount(userId);
    }

    @GetMapping("/{id}")
    public AccountResponse getById(@PathVariable String id) {
        return accountService.getById(id);
    }

    @GetMapping("/user/{userId}")
    public List<AccountResponse> getByUser(@PathVariable Long userId) {
        return accountService.getByUser(userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        accountService.delete(id);
    }

    @PostMapping("/{id}/deposit")
    public TransactionResponse deposit(@PathVariable String id,
                                        @Valid @RequestBody OperationRequest request) {
        return transactionService.deposit(id, request);
    }

    @PostMapping("/{id}/withdraw")
    public TransactionResponse withdraw(@PathVariable String id,
                                         @Valid @RequestBody OperationRequest request) {
        return transactionService.withdraw(id, request);
    }

    @PostMapping("/{id}/transfer")
    public TransactionResponse transfer(@PathVariable String id,
                                         @Valid @RequestBody TransferRequest request) {
        return transactionService.transfer(id, request);
    }

    @GetMapping("/{id}/transactions")
    public List<TransactionResponse> getTransactions(@PathVariable String id) {
        return transactionService.getByAccount(id);
    }
}
