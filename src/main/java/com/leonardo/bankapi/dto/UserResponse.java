package com.leonardo.bankapi.dto;

import lombok.Value;

@Value
public class UserResponse {
    Long id;
    String name;
    String email;
}
