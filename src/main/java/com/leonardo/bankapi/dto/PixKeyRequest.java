package com.leonardo.bankapi.dto;

import com.leonardo.bankapi.entity.PixKeyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PixKeyRequest {

    @NotNull(message = "Tipo de chave é obrigatório")
    private PixKeyType type;

    /** Obrigatório para EMAIL, CPF e PHONE. Para RANDOM, deixar em branco. */
    private String keyValue;

    @NotBlank(message = "ID da conta é obrigatório")
    private String accountId;
}
