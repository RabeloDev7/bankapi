package com.leonardo.bankapi.repository;

import com.leonardo.bankapi.entity.Account;
import com.leonardo.bankapi.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositório para transações
 */
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByAccountOrderByCreatedAtDesc(Account account);
}
