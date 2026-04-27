package com.leonardo.bankapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Chave PIX vinculada a uma conta.
 * Tipos suportados: EMAIL, CPF, PHONE, RANDOM
 */
@Entity
@Table(name = "pix_keys",
       uniqueConstraints = @UniqueConstraint(columnNames = "key_value"))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PixKey {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PixKeyType type;

    @Column(name = "key_value", nullable = false, unique = true)
    private String keyValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        if (this.id == null)        this.id        = UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}
