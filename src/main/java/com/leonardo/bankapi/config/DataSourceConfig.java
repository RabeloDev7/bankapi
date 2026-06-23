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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuracao do DataSource para producao.
 *
 * Usa regex para parsear DATABASE_URL pois URI.create() falha
 * quando a senha contem caracteres especiais (@, !, #, etc.).
 *
 * Suporta os formatos:
 *   postgresql://user:pass@host:port/db
 *   postgres://user:pass@host:port/db
 *   jdbc:postgresql://user:pass@host:port/db
 */
@Configuration
@Profile("prod")
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    // Regex que captura user, senha (incluindo chars especiais), host, porta e banco
    // A senha usa (.+?) lazy antes do ultimo @ para lidar com @ na senha
    private static final Pattern DB_URL_PATTERN = Pattern.compile(
        "(?:jdbc:)?(?:postgres(?:ql)?://)([^:]+):(.+)@([^:@/]+)(?::(\\d+))?/(.+)"
    );

    @Bean
    @Primary
    public DataSource dataSource() {
        String rawUrl = System.getenv("DATABASE_URL");

        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalStateException(
                "[DataSourceConfig] DATABASE_URL nao configurada! " +
                "Defina a variavel de ambiente no dashboard do Render.");
        }

        // Remove query string (?sslmode=require etc) — vamos adicionar via propriedade Hikari
        String cleanUrl = rawUrl.contains("?") ? rawUrl.substring(0, rawUrl.indexOf('?')) : rawUrl;

        Matcher matcher = DB_URL_PATTERN.matcher(cleanUrl);
        if (!matcher.matches()) {
            throw new IllegalStateException(
                "[DataSourceConfig] Formato de DATABASE_URL invalido: " + cleanUrl +
                "\nEsperado: postgresql://user:pass@host:port/db");
        }

        String username = matcher.group(1);
        String password = matcher.group(2);
        String host     = matcher.group(3);
        String portStr  = matcher.group(4);
        String dbName   = matcher.group(5);
        int    port     = (portStr != null) ? Integer.parseInt(portStr) : 5432;

        // Monta JDBC URL limpa sem credenciais
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);

        log.info("==> DataSource conectando em: {}:{}/{}", host, port, dbName);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        // SSL obrigatorio para Supabase
        config.addDataSourceProperty("sslmode", "require");

        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }
}
