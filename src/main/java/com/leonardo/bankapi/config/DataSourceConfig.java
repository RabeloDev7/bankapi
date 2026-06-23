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
import java.net.URI;

/**
 * Configuracao do DataSource para producao.
 * Converte DATABASE_URL (postgresql://) para JDBC,
 * preservando parametros SSL necessarios para o Supabase.
 */
@Configuration
@Profile("prod")
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    @Primary
    public DataSource dataSource() {
        String rawUrl = System.getenv("DATABASE_URL");

        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalStateException(
                "[DataSourceConfig] DATABASE_URL nao configurada!");
        }

        // Remove prefixo jdbc: se ja vier assim
        String normalized = rawUrl.startsWith("jdbc:") ? rawUrl.substring(5) : rawUrl;

        // Normaliza postgres:// para postgresql://
        if (normalized.startsWith("postgres://")) {
            normalized = "postgresql://" + normalized.substring("postgres://".length());
        }

        // Extrai credenciais via URI (sem query string)
        String withoutQuery = normalized.contains("?")
                ? normalized.substring(0, normalized.indexOf('?'))
                : normalized;

        URI uri = URI.create(withoutQuery);

        String host     = uri.getHost();
        int    port     = uri.getPort();
        String dbName   = uri.getPath();
        String userInfo = uri.getUserInfo();

        String username = null;
        String password = null;
        if (userInfo != null && userInfo.contains(":")) {
            int sep  = userInfo.indexOf(':');
            username = userInfo.substring(0, sep);
            password = userInfo.substring(sep + 1);
        }

        // Monta JDBC URL com SSL obrigatorio (necessario para Supabase)
        String jdbcUrl = "jdbc:postgresql://" + host
                + (port != -1 ? ":" + port : "")
                + dbName
                + "?sslmode=require";

        log.info("==> DataSource conectando em: {}:{}{}", host,
                port != -1 ? port : 5432, dbName);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        if (username != null) config.setUsername(username);
        if (password != null) config.setPassword(password);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }
}
