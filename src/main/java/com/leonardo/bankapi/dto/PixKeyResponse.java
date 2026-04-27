package com.leonardo.bankapi.dto;

import com.leonardo.bankapi.entity.PixKey;
import com.leonardo.bankapi.entity.PixKeyType;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class PixKeyResponse {
    String id;
    PixKeyType type;
    String keyValue;
    String accountId;
    LocalDateTime createdAt;

    public PixKeyResponse(PixKey key) {
        this.id        = key.getId();
        this.type      = key.getType();
        this.keyValue  = key.getKeyValue();
        this.accountId = key.getAccount().getId();
        this.createdAt = key.getCreatedAt();
    }
}
