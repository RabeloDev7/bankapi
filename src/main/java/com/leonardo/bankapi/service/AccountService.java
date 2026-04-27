package com.leonardo.bankapi.service;

import com.leonardo.bankapi.dto.AccountResponse;
import com.leonardo.bankapi.entity.Account;
import com.leonardo.bankapi.entity.User;
import com.leonardo.bankapi.exception.ResourceNotFoundException;
import com.leonardo.bankapi.repository.AccountRepository;
import com.leonardo.bankapi.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public AccountResponse createAccount(Long userId) {
        User user = findUser(userId);
        Account account = Account.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .build();
        return toResponse(accountRepository.save(account));
    }

    public AccountResponse getById(String accountId) {
        return toResponse(findAccount(accountId));
    }

    public List<AccountResponse> getByUser(Long userId) {
        User user = findUser(userId);
        return accountRepository.findByUser(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void delete(String accountId) {
        if (!accountRepository.existsById(accountId))
            throw new ResourceNotFoundException("Conta não encontrada: " + accountId);
        accountRepository.deleteById(accountId);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    public Account findAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + accountId));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));
    }

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getBalance(),
                account.getUser().getId(),
                account.getUser().getName()
        );
    }
}
