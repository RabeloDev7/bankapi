package com.leonardo.bankapi.repository;

import com.leonardo.bankapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório para operações com usuários
 */
public interface UserRepository extends JpaRepository<User, Long> {
}