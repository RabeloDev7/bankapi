package com.leonardo.bankapi.dto;

import com.leonardo.bankapi.entity.PixKey;
import com.leonardo.bankapi.entity.PixKeyType;

import java.time.LocalDateTime;

public class PixKeyResponse {

    private String id;
    private PixKeyType type;
    private String keyValue;
    private String accountId;
    private LocalDateTime createdAt;

    public PixKeyResponse(PixKey key) {
        this.id        = key.getId();
        this.type      = key.getType();
        this.keyValue  = key.getKeyValue();
        this.accountId = key.getAccount().getId();
        this.createdAt = key.getCreatedAt();
    }

    public String getId() { return id; }
    public PixKeyType getType() { return type; }
    public String getKeyValue() { return keyValue; }
    public String getAccountId() { return accountId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
