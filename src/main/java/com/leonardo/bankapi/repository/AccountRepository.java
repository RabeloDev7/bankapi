package com.leonardo.bankapi.repository;

import com.leonardo.bankapi.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório para contas bancárias
 */
public interface AccountRepository extends JpaRepository<Account, String> {
}

