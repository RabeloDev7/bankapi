package com.leonardo.bankapi.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Representa uma conta bancária
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    private String id;

    /**
     * Saldo da conta
     */
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Usuário dono da conta
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Account() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Atualiza o saldo da conta
     */
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public User getUser() {
        return user;
    }

    /**
     * Define o usuário dono da conta
     */
    public void setUser(User user) {
        this.user = user;
    }
}