package com.ifo.backend;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

// PostgreSQL 15 descartável (mesma versão do docker-compose) para os testes de integração.
// O container é estático e iniciado uma única vez: o contexto do Cucumber e o do
// IfoBackendApplicationTests compartilham o mesmo banco em vez de subir um por contexto.
@TestConfiguration(proxyBeanMethods = false)
public class PostgresTestConfig {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15");

    static {
        POSTGRES.start();
    }

    // @ServiceConnection faz o Spring apontar o DataSource para este container
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgres() {
        return POSTGRES;
    }
}
