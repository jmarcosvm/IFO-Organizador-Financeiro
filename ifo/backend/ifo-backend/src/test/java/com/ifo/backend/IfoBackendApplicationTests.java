package com.ifo.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

// Sobe o contexto inteiro e valida (ddl-auto=validate) que as entidades batem com o esquema do Flyway
@SpringBootTest
@ActiveProfiles("teste")
@Import(PostgresTestConfig.class)
class IfoBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
