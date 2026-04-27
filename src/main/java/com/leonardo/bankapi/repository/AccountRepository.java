package com.leonardo.bankapi.repository;

import com.leonardo.bankapi.entity.Account;
import com.leonardo.bankapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositório para contas bancárias
 */
public interface AccountRepository extends JpaRepository<Account, String> {

    List<Account> findByUser(User user);
}
