package com.leonardo.bankapi.service;

import com.leonardo.bankapi.dto.OperationRequest;
import com.leonardo.bankapi.dto.TransactionResponse;
import com.leonardo.bankapi.dto.TransferRequest;
import com.leonardo.bankapi.entity.Account;
import com.leonardo.bankapi.entity.Transaction;
import com.leonardo.bankapi.entity.User;
import com.leonardo.bankapi.exception.BusinessException;
import com.leonardo.bankapi.exception.ResourceNotFoundException;
import com.leonardo.bankapi.repository.AccountRepository;
import com.leonardo.bankapi.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService — testes unitários")
class TransactionServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock TransactionRepository transactionRepository;
    @InjectMocks TransactionService transactionService;

    private Account accountA;
    private Account accountB;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1L).name("Leo").email("leo@email.com").password("x").build();

        accountA = Account.builder()
                .id("acc-a")
                .balance(new BigDecimal("500.00"))
                .user(user)
                .build();

        accountB = Account.builder()
                .id("acc-b")
                .balance(new BigDecimal("100.00"))
                .user(user)
                .build();
    }

    // ── deposit ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deposit: saldo aumenta corretamente")
    void deposit_success() {
        when(accountRepository.findById("acc-a")).thenReturn(Optional.of(accountA));
        when(transactionRepository.save(any())).thenAnswer(inv -> {
            Transaction tx = inv.getArgument(0);
            // simula @PrePersist
            java.lang.reflect.Field id;
            try {
                id = Transaction.class.getDeclaredField("id");
                id.setAccessible(true);
                id.set(tx, "tx-1");
                java.lang.reflect.Field createdAt = Transaction.class.getDeclaredField("createdAt");
                createdAt.setAccessible(true);
                createdAt.set(tx, LocalDateTime.now());
            } catch (Exception ignored) {}
            return tx;
        });

        OperationRequest req = new OperationRequest();
        req.setAmount(new BigDecimal("200.00"));

        TransactionResponse response = transactionService.deposit("acc-a", req);

        assertThat(accountA.getBalance()).isEqualByComparingTo("700.00");
        assertThat(response.getType()).isEqualTo("DEPOSIT");
        assertThat(response.getAmount()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("deposit: conta inexistente lança ResourceNotFoundException")
    void deposit_accountNotFound() {
        when(accountRepository.findById("nope")).thenReturn(Optional.empty());
        OperationRequest req = new OperationRequest();
        req.setAmount(BigDecimal.TEN);

        assertThatThrownBy(() -> transactionService.deposit("nope", req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── withdraw ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("withdraw: saldo diminui corretamente")
    void withdraw_success() {
        when(accountRepository.findById("acc-a")).thenReturn(Optional.of(accountA));
        when(transactionRepository.save(any())).thenAnswer(inv -> stubTransaction(inv.getArgument(0)));

        OperationRequest req = new OperationRequest();
        req.setAmount(new BigDecimal("100.00"));

        transactionService.withdraw("acc-a", req);

        assertThat(accountA.getBalance()).isEqualByComparingTo("400.00");
    }

    @Test
    @DisplayName("withdraw: saldo insuficiente lança BusinessException")
    void withdraw_insuficientBalance() {
        when(accountRepository.findById("acc-a")).thenReturn(Optional.of(accountA));
        OperationRequest req = new OperationRequest();
        req.setAmount(new BigDecimal("999.00"));

        assertThatThrownBy(() -> transactionService.withdraw("acc-a", req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Saldo insuficiente");
    }

    // ── transfer ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("transfer: saldos atualizados corretamente")
    void transfer_success() {
        when(accountRepository.findById("acc-a")).thenReturn(Optional.of(accountA));
        when(accountRepository.findById("acc-b")).thenReturn(Optional.of(accountB));
        when(transactionRepository.save(any())).thenAnswer(inv -> stubTransaction(inv.getArgument(0)));

        TransferRequest req = new TransferRequest();
        req.setToAccountId("acc-b");
        req.setAmount(new BigDecimal("200.00"));

        transactionService.transfer("acc-a", req);

        assertThat(accountA.getBalance()).isEqualByComparingTo("300.00");
        assertThat(accountB.getBalance()).isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("transfer: mesma conta lança IllegalArgumentException")
    void transfer_sameAccount() {
        TransferRequest req = new TransferRequest();
        req.setToAccountId("acc-a");
        req.setAmount(BigDecimal.TEN);

        assertThatThrownBy(() -> transactionService.transfer("acc-a", req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("origem e destino não podem ser iguais");
    }

    @Test
    @DisplayName("transfer: saldo insuficiente lança BusinessException")
    void transfer_insufficientBalance() {
        when(accountRepository.findById("acc-a")).thenReturn(Optional.of(accountA));
        when(accountRepository.findById("acc-b")).thenReturn(Optional.of(accountB));

        TransferRequest req = new TransferRequest();
        req.setToAccountId("acc-b");
        req.setAmount(new BigDecimal("9999.00"));

        assertThatThrownBy(() -> transactionService.transfer("acc-a", req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Saldo insuficiente");
    }

    // ── getByAccount ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByAccount: retorna lista ordenada")
    void getByAccount_returnsList() {
        when(accountRepository.findById("acc-a")).thenReturn(Optional.of(accountA));
        Transaction tx = new Transaction();
        tx.setAccount(accountA);
        tx.setAmount(BigDecimal.TEN);
        tx.setType("DEPOSIT");
        stubTransaction(tx);
        when(transactionRepository.findByAccountOrderByCreatedAtDesc(accountA)).thenReturn(List.of(tx));

        List<TransactionResponse> result = transactionService.getByAccount("acc-a");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo("DEPOSIT");
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private Transaction stubTransaction(Transaction tx) {
        try {
            var idField = Transaction.class.getDeclaredField("id");
            idField.setAccessible(true);
            if (idField.get(tx) == null) idField.set(tx, java.util.UUID.randomUUID().toString());

            var caField = Transaction.class.getDeclaredField("createdAt");
            caField.setAccessible(true);
            if (caField.get(tx) == null) caField.set(tx, LocalDateTime.now());
        } catch (Exception ignored) {}
        return tx;
    }
}
