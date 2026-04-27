package com.leonardo.bankapi.service;

import com.leonardo.bankapi.dto.OperationRequest;
import com.leonardo.bankapi.dto.TransactionResponse;
import com.leonardo.bankapi.dto.TransferRequest;
import com.leonardo.bankapi.entity.Account;
import com.leonardo.bankapi.entity.Transaction;
import com.leonardo.bankapi.exception.BusinessException;
import com.leonardo.bankapi.exception.ResourceNotFoundException;
import com.leonardo.bankapi.repository.AccountRepository;
import com.leonardo.bankapi.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository,
                               AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public TransactionResponse deposit(String accountId, OperationRequest request) {
        Account account = findAccount(accountId);
        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);
        return toResponse(save(account, request.getAmount(), "DEPOSIT", request.getDescription()));
    }

    @Transactional
    public TransactionResponse withdraw(String accountId, OperationRequest request) {
        Account account = findAccount(accountId);
        checkBalance(account, request.getAmount());
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);
        return toResponse(save(account, request.getAmount(), "WITHDRAW", request.getDescription()));
    }

    @Transactional
    public TransactionResponse transfer(String fromAccountId, TransferRequest request) {
        if (fromAccountId.equals(request.getToAccountId())) {
            throw new IllegalArgumentException("Conta de origem e destino não podem ser iguais");
        }

        Account from = findAccount(fromAccountId);
        Account to   = findAccount(request.getToAccountId());

        checkBalance(from, request.getAmount());

        from.setBalance(from.getBalance().subtract(request.getAmount()));
        to.setBalance(to.getBalance().add(request.getAmount()));
        accountRepository.save(from);
        accountRepository.save(to);

        return toResponse(save(from, request.getAmount(), "TRANSFER", request.getDescription()));
    }

    public List<TransactionResponse> getByAccount(String accountId) {
        Account account = findAccount(accountId);
        return transactionRepository.findByAccountOrderByCreatedAtDesc(account)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    Account findAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + accountId));
    }

    private void checkBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Saldo insuficiente");
        }
    }

    private Transaction save(Account account, BigDecimal amount, String type, String description) {
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setDescription(description);
        return transactionRepository.save(tx);
    }

    TransactionResponse toResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getAccount().getId(),
                tx.getAmount(),
                tx.getType(),
                tx.getDescription(),
                tx.getCreatedAt()
        );
    }
}
