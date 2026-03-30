package com.leonardo.bankapi.repository;

import com.leonardo.bankapi.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório para transações
 */
public interface TransactionRepository extends JpaRepository<Transaction, String> {
}