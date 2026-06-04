package com.leonardo.bankapi.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Configuracao do DataSource para producao (Render).
 *
 * O Render fornece DATABASE_URL no formato:
 *   postgresql://user:pass@host/db
 *
 * O JDBC precisa de:
 *   jdbc:postgresql://user:pass@host/db
 *
 * Esta classe faz a conversao automaticamente via variavel de ambiente,
 * sem depender de scripts shell ou properties intermediarias.
 */
@Configuration
@Profile("prod")
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    @Primary
    public DataSource dataSource() {
        String rawUrl = System.getenv("DATABASE_URL");
        String jdbcUrl = toJdbcUrl(rawUrl);

        log.info("==> DataSource configurado para producao");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }

    private String toJdbcUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException(
                "[DataSourceConfig] DATABASE_URL nao esta configurada! " +
                "Defina a variavel de ambiente no dashboard do Render.");
        }
        if (url.startsWith("jdbc:")) {
            return url;
        }
        if (url.startsWith("postgresql://")) {
            return "jdbc:" + url;
        }
        if (url.startsWith("postgres://")) {
            return "jdbc:postgresql://" + url.substring("postgres://".length());
        }
        throw new IllegalStateException(
            "[DataSourceConfig] Formato de DATABASE_URL nao reconhecido: " + url +
            ". Esperado: postgresql://user:pass@host/db");
    }
}
