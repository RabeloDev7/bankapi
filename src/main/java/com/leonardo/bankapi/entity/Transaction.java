package com.leonardo.bankapi.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa uma transação bancária
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    private String id;

    /**
     * Conta associada à transação
     */
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    /**
     * Valor da transação
     */
    private BigDecimal amount;

    /**
     * Tipo da transação (DEPOSIT, WITHDRAW)
     */
    private String type;

    /**
     * Data e hora da transação
     */
    private LocalDateTime createdAt;

    public Transaction() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}