package com.leonardo.bankapi.dto;

import com.leonardo.bankapi.entity.PixKeyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PixKeyRequest {

    @NotNull(message = "Tipo de chave é obrigatório")
    private PixKeyType type;

    /** Obrigatório apenas para EMAIL, CPF e PHONE. Para RANDOM, deixar em branco. */
    private String keyValue;

    @NotBlank(message = "ID da conta é obrigatório")
    private String accountId;

    public PixKeyType getType() { return type; }
    public void setType(PixKeyType type) { this.type = type; }

    public String getKeyValue() { return keyValue; }
    public void setKeyValue(String keyValue) { this.keyValue = keyValue; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
}
