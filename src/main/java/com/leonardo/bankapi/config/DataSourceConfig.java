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
 * Configuracao do DataSource para producao (Render).
 *
 * O Render fornece DATABASE_URL no formato:
 *   postgresql://user:pass@host/db
 *
 * Esta classe converte para JDBC e extrai usuario/senha
 * explicitamente para o HikariCP — abordagem mais robusta
 * do que embedar credenciais na URL.
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
                "[DataSourceConfig] DATABASE_URL nao esta configurada! " +
                "Defina a variavel de ambiente no dashboard do Render.");
        }

        // Normaliza para postgresql:// (remove prefixo jdbc: se vier assim)
        String normalizedUrl = rawUrl;
        if (normalizedUrl.startsWith("jdbc:")) {
            normalizedUrl = normalizedUrl.substring(5);
        }
        if (normalizedUrl.startsWith("postgres://")) {
            normalizedUrl = "postgresql://" + normalizedUrl.substring("postgres://".length());
        }

        // Faz parse da URI para extrair host, porta, banco, usuario e senha
        URI uri = URI.create(normalizedUrl);

        String host     = uri.getHost();
        int    port     = uri.getPort(); // -1 se nao especificado
        String dbName   = uri.getPath(); // "/bankapi_db"
        String userInfo = uri.getUserInfo(); // "user:pass"

        String username = null;
        String password = null;
        if (userInfo != null && userInfo.contains(":")) {
            int sep  = userInfo.indexOf(':');
            username = userInfo.substring(0, sep);
            password = userInfo.substring(sep + 1);
        }

        // Monta JDBC URL sem credenciais (mais seguro e mais compativel com HikariCP)
        String jdbcUrl = "jdbc:postgresql://" + host
                + (port != -1 ? ":" + port : "")
                + dbName;

        log.info("==> DataSource: jdbc:postgresql://{}:{}{}", host,
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
