package com.leonardo.bankapi.dto;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
public class TransactionResponse {
    String id;
    String accountId;
    BigDecimal amount;
    String type;
    String description;
    LocalDateTime createdAt;
}
