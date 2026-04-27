package com.leonardo.bankapi.dto;

import java.math.BigDecimal;

public class AccountResponse {

    private String id;
    private BigDecimal balance;
    private Long userId;
    private String userName;

    public AccountResponse(String id, BigDecimal balance, Long userId, String userName) {
        this.id = id;
        this.balance = balance;
        this.userId = userId;
        this.userName = userName;
    }

    public String getId() { return id; }
    public BigDecimal getBalance() { return balance; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
}
