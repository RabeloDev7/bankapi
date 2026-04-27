package com.leonardo.bankapi.service;

import com.leonardo.bankapi.dto.*;
import com.leonardo.bankapi.entity.*;
import com.leonardo.bankapi.exception.BusinessException;
import com.leonardo.bankapi.exception.ResourceNotFoundException;
import com.leonardo.bankapi.repository.AccountRepository;
import com.leonardo.bankapi.repository.PixKeyRepository;
import com.leonardo.bankapi.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PixService {

    private final PixKeyRepository pixKeyRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public PixService(PixKeyRepository pixKeyRepository,
                      AccountRepository accountRepository,
                      TransactionRepository transactionRepository) {
        this.pixKeyRepository     = pixKeyRepository;
        this.accountRepository    = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // ── Gerenciamento de chaves

    @Transactional
    public PixKeyResponse registerKey(PixKeyRequest request) {
        Account account = findAccount(request.getAccountId());

        String keyValue = resolveKeyValue(request);

        if (pixKeyRepository.existsByKeyValue(keyValue)) {
            throw new BusinessException("Chave PIX já cadastrada: " + keyValue);
        }

        PixKey pixKey = PixKey.builder()
                .type(request.getType())
                .keyValue(keyValue)
                .account(account)
                .build();

        return new PixKeyResponse(pixKeyRepository.save(pixKey));
    }

    public List<PixKeyResponse> getKeysByAccount(String accountId) {
        Account account = findAccount(accountId);
        return pixKeyRepository.findByAccount(account).stream()
                .map(PixKeyResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteKey(String keyId) {
        if (!pixKeyRepository.existsById(keyId)) {
            throw new ResourceNotFoundException("Chave PIX não encontrada: " + keyId);
        }
        pixKeyRepository.deleteById(keyId);
    }

    // ── Envio PIX ───────────────────────────────────────────────────────────

    @Transactional
    public TransactionResponse send(String fromAccountId, PixSendRequest request) {
        if (fromAccountId == null || fromAccountId.isBlank()) {
            throw new IllegalArgumentException("Conta de origem é obrigatória");
        }

        Account from = findAccount(fromAccountId);

        PixKey targetKey = pixKeyRepository.findByKeyValue(request.getPixKey())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chave PIX não encontrada: " + request.getPixKey()));

        Account to = targetKey.getAccount();

        if (from.getId().equals(to.getId())) {
            throw new BusinessException("Não é possível enviar PIX para a própria conta");
        }

        if (from.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("Saldo insuficiente para realizar o PIX");
        }

        from.setBalance(from.getBalance().subtract(request.getAmount()));
        to.setBalance(to.getBalance().add(request.getAmount()));

        accountRepository.save(from);
        accountRepository.save(to);

        // Registra transação de saída (PIX_SENT) na conta de origem
        Transaction sent = buildTransaction(from, request.getAmount(), "PIX_SENT", request.getDescription());
        transactionRepository.save(sent);

        // Registra transação de entrada (PIX_RECEIVED) na conta de destino
        Transaction received = buildTransaction(to, request.getAmount(), "PIX_RECEIVED", request.getDescription());
        transactionRepository.save(received);

        return toTransactionResponse(sent);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private Account findAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + accountId));
    }

    private String resolveKeyValue(PixKeyRequest request) {
        if (request.getType() == PixKeyType.RANDOM) {
            return UUID.randomUUID().toString();
        }
        String value = request.getKeyValue();
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Valor da chave é obrigatório para o tipo " + request.getType());
        }
        return value.trim().toLowerCase();
    }

    private Transaction buildTransaction(Account account, java.math.BigDecimal amount,
                                          String type, String description) {
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setDescription(description);
        return tx;
    }

    private TransactionResponse toTransactionResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getAccount().getId(),
                tx.getAmount(),
                tx.getType(),
                tx.getDescription(),
                tx.getCreatedAt()
        );
    }
}
