package com.leonardo.bankapi.service;

import com.leonardo.bankapi.dto.PixKeyRequest;
import com.leonardo.bankapi.dto.PixKeyResponse;
import com.leonardo.bankapi.dto.PixSendRequest;
import com.leonardo.bankapi.dto.TransactionResponse;
import com.leonardo.bankapi.entity.*;
import com.leonardo.bankapi.exception.BusinessException;
import com.leonardo.bankapi.exception.ResourceNotFoundException;
import com.leonardo.bankapi.repository.AccountRepository;
import com.leonardo.bankapi.repository.PixKeyRepository;
import com.leonardo.bankapi.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PixService — testes unitários")
class PixServiceTest {

    @Mock PixKeyRepository pixKeyRepository;
    @Mock AccountRepository accountRepository;
    @Mock TransactionRepository transactionRepository;
    @InjectMocks PixService pixService;

    private Account accountA;
    private Account accountB;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1L).name("Leo").email("leo@email.com").password("x").build();

        accountA = Account.builder()
                .id("acc-a").balance(new BigDecimal("1000.00")).user(user).build();

        accountB = Account.builder()
                .id("acc-b").balance(new BigDecimal("50.00")).user(user).build();
    }

    // ── registerKey ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("registerKey EMAIL: cadastra com sucesso")
    void registerKey_email_success() {
        when(accountRepository.findById("acc-a")).thenReturn(Optional.of(accountA));
        when(pixKeyRepository.existsByKeyValue("leo@email.com")).thenReturn(false);
        when(pixKeyRepository.save(any())).thenAnswer(inv -> {
            PixKey k = inv.getArgument(0);
            stubPixKey(k);
            return k;
        });

        PixKeyRequest req = new PixKeyRequest();
        req.setType(PixKeyType.EMAIL);
        req.setKeyValue("leo@email.com");
        req.setAccountId("acc-a");

        PixKeyResponse response = pixService.registerKey(req);

        assertThat(response.getType()).isEqualTo(PixKeyType.EMAIL);
        assertThat(response.getKeyValue()).isEqualTo("leo@email.com");
    }

    @Test
    @DisplayName("registerKey RANDOM: gera UUID automaticamente")
    void registerKey_random_generatesUUID() {
        when(accountRepository.findById("acc-a")).thenReturn(Optional.of(accountA));
        when(pixKeyRepository.existsByKeyValue(any())).thenReturn(false);
        when(pixKeyRepository.save(any())).thenAnswer(inv -> {
            PixKey k = inv.getArgument(0);
            stubPixKey(k);
            return k;
        });

        PixKeyRequest req = new PixKeyRequest();
        req.setType(PixKeyType.RANDOM);
        req.setAccountId("acc-a");

        PixKeyResponse response = pixService.registerKey(req);

        assertThat(response.getKeyValue()).isNotBlank();
        // UUID gerado automaticamente — deve ter 36 chars
        assertThat(response.getKeyValue()).hasSize(36);
    }

    @Test
    @DisplayName("registerKey: chave duplicada lança BusinessException")
    void registerKey_duplicate() {
        when(accountRepository.findById("acc-a")).thenReturn(Optional.of(accountA));
        when(pixKeyRepository.existsByKeyValue("leo@email.com")).thenReturn(true);

        PixKeyRequest req = new PixKeyRequest();
        req.setType(PixKeyType.EMAIL);
        req.setKeyValue("leo@email.com");
        req.setAccountId("acc-a");

        assertThatThrownBy(() -> pixService.registerKey(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Chave PIX já cadastrada");
    }

    @Test
    @DisplayName("registerKey: tipo não-RANDOM sem valor lança IllegalArgumentException")
    void registerKey_missingValue() {
        when(accountRepository.findById("acc-a")).thenReturn(Optional.of(accountA));

        PixKeyRequest req = new PixKeyRequest();
        req.setType(PixKeyType.CPF);
        req.setKeyValue("");
        req.setAccountId("acc-a");

        assertThatThrownBy(() -> pixService.registerKey(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Valor da chave é obrigatório");
    }

    // ── getKeysByAccount ────────────────────────────────────────────────────

    @Test
    @DisplayName("getKeysByAccount: retorna lista de chaves")
    void getKeysByAccount_returnsList() {
        when(accountRepository.findById("acc-a")).thenReturn(Optional.of(accountA));

        PixKey key = PixKey.builder()
                .type(PixKeyType.EMAIL).keyValue("leo@email.com").account(accountA).build();
        stubPixKey(key);

        when(pixKeyRepository.findByAccount(accountA)).thenReturn(List.of(key));

        List<PixKeyResponse> result = pixService.getKeysByAccount("acc-a");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKeyValue()).isEqualTo("leo@email.com");
    }

    // ── deleteKey ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteKey: sucesso")
    void deleteKey_success() {
        when(pixKeyRepository.existsById("key-1")).thenReturn(true);
        assertThatCode(() -> pixService.deleteKey("key-1")).doesNotThrowAnyException();
        verify(pixKeyRepository).deleteById("key-1");
    }

    @Test
    @DisplayName("deleteKey: chave inexistente lança ResourceNotFoundException")
    void deleteKey_notFound() {
        when(pixKeyRepository.existsById("nope")).thenReturn(false);
        assertThatThrownBy(() -> pixService.deleteKey("nope"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── send ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("send: transfere saldo e gera transações de saída e entrada")
    void send_success() {
        PixKey targetKey = PixKey.builder()
                .type(PixKeyType.EMAIL).keyValue("destino@email.com").account(accountB).build();
        stubPixKey(targetKey);

        when(accountRepository.findById("acc-a")).thenReturn(Optional.of(accountA));
        when(pixKeyRepository.findByKeyValue("destino@email.com")).thenReturn(Optional.of(targetKey));
        when(transactionRepository.save(any())).thenAnswer(inv -> stubTx(inv.getArgument(0)));

        PixSendRequest req = new PixSendRequest();
        req.setPixKey("destino@email.com");
        req.setAmount(new BigDecimal("300.00"));
        req.setDescription("Aluguel");

        TransactionResponse response = pixService.send("acc-a", req);

        assertThat(accountA.getBalance()).isEqualByComparingTo("700.00");
        assertThat(accountB.getBalance()).isEqualByComparingTo("350.00");
        assertThat(response.getType()).isEqualTo("PIX_SENT");
        assertThat(response.getDescription()).isEqualTo("Aluguel");
        // Deve salvar 2 transações: PIX_SENT e PIX_RECEIVED
        verify(transactionRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("send: saldo insuficiente lança BusinessException")
    void send_insufficientBalance() {
        PixKey targetKey = PixKey.builder()
                .type(PixKeyType.EMAIL).keyValue("destino@email.com").account(accountB).build();
        stubPixKey(targetKey);

        when(accountRepository.findById("acc-a")).thenReturn(Optional.of(accountA));
        when(pixKeyRepository.findByKeyValue("destino@email.com")).thenReturn(Optional.of(targetKey));

        PixSendRequest req = new PixSendRequest();
        req.setPixKey("destino@email.com");
        req.setAmount(new BigDecimal("9999.00"));

        assertThatThrownBy(() -> pixService.send("acc-a", req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Saldo insuficiente");
    }

    @Test
    @DisplayName("send: PIX para a própria conta lança BusinessException")
    void send_sameAccount() {
        PixKey ownKey = PixKey.builder()
                .type(PixKeyType.EMAIL).keyValue("proprio@email.com").account(accountA).build();
        stubPixKey(ownKey);

        when(accountRepository.findById("acc-a")).thenReturn(Optional.of(accountA));
        when(pixKeyRepository.findByKeyValue("proprio@email.com")).thenReturn(Optional.of(ownKey));

        PixSendRequest req = new PixSendRequest();
        req.setPixKey("proprio@email.com");
        req.setAmount(BigDecimal.TEN);

        assertThatThrownBy(() -> pixService.send("acc-a", req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("própria conta");
    }

    @Test
    @DisplayName("send: chave PIX inexistente lança ResourceNotFoundException")
    void send_pixKeyNotFound() {
        when(accountRepository.findById("acc-a")).thenReturn(Optional.of(accountA));
        when(pixKeyRepository.findByKeyValue("naoexiste@email.com")).thenReturn(Optional.empty());

        PixSendRequest req = new PixSendRequest();
        req.setPixKey("naoexiste@email.com");
        req.setAmount(BigDecimal.TEN);

        assertThatThrownBy(() -> pixService.send("acc-a", req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Chave PIX não encontrada");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void stubPixKey(PixKey key) {
        try {
            var idField = PixKey.class.getDeclaredField("id");
            idField.setAccessible(true);
            if (idField.get(key) == null)
                idField.set(key, java.util.UUID.randomUUID().toString());

            var caField = PixKey.class.getDeclaredField("createdAt");
            caField.setAccessible(true);
            if (caField.get(key) == null)
                caField.set(key, LocalDateTime.now());
        } catch (Exception ignored) {}
    }

    private Transaction stubTx(Transaction tx) {
        try {
            var idField = Transaction.class.getDeclaredField("id");
            idField.setAccessible(true);
            if (idField.get(tx) == null) idField.set(tx, java.util.UUID.randomUUID().toString());

            var caField = Transaction.class.getDeclaredField("createdAt");
            caField.setAccessible(true);
            if (caField.get(tx) == null) caField.set(tx, LocalDateTime.now());
        } catch (Exception ignored) {}
        return tx;
    }
}
