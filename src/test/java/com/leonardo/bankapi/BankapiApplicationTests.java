package com.leonardo.bankapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BankapiApplicationTests {

    @Test
    void contextLoads() {
        // Garante que o contexto Spring sobe sem erros com H2
    }
}
