package com.leonardo.bankapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
            "app",     "BankAPI",
            "version", "1.0.0",
            "status",  "online",
            "docs",    "https://github.com/RabeloDev7/bankapi"
        );
    }
}
