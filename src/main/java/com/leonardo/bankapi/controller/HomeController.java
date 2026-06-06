package com.leonardo.bankapi.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class HomeController {

    /** Redireciona a raiz para o frontend */
    @GetMapping("/")
    public void home(HttpServletResponse response) throws IOException {
        response.sendRedirect("/bankapi/pages/login.html");
    }

    /** Endpoint de status da API */
    @GetMapping("/api")
    public Map<String, String> status() {
        return Map.of(
            "app",     "BankAPI",
            "version", "1.0.0",
            "status",  "online",
            "docs",    "https://github.com/RabeloDev7/bankapi"
        );
    }
}
