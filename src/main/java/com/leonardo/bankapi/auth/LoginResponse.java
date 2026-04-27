package com.leonardo.bankapi.auth;

import lombok.Value;

@Value
public class LoginResponse {
    String token;
    String email;
    String name;
}
