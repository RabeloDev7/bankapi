package com.leonardo.bankapi.repository;

import com.leonardo.bankapi.entity.Account;
import com.leonardo.bankapi.entity.PixKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PixKeyRepository extends JpaRepository<PixKey, String> {

    Optional<PixKey> findByKeyValue(String keyValue);

    List<PixKey> findByAccount(Account account);

    boolean existsByKeyValue(String keyValue);
}
