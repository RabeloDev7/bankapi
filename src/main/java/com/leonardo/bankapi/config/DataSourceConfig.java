package com.leonardo.bankapi.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
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
 * Esta classe faz a conversao automaticamente,
 * eliminando a necessidade de scripts shell.
 */
@Configuration
@Profile("prod")
public class DataSourceConfig {

    @Value("${DATABASE_URL}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        String jdbcUrl = toJdbcUrl(databaseUrl);

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
                "DATABASE_URL nao configurada. Defina a variavel de ambiente no Render.");
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
        throw new IllegalStateException("Formato de DATABASE_URL nao reconhecido: " + url);
    }
}
