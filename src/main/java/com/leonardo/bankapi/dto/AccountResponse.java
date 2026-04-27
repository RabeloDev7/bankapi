package com.leonardo.bankapi.dto;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class AccountResponse {
    String id;
    BigDecimal balance;
    Long userId;
    String userName;
}
