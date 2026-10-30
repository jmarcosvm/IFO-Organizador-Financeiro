package com.ifo.backend.steps;

import com.ifo.backend.IfoBackendApplication;
import com.ifo.backend.PostgresTestConfig;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

// Único ponto com @SpringBootTest; as classes de steps só usam @Autowired.
// Os cenários chamam a API por HTTP (MockMvc), passando pelo filtro JWT, pela validação e
// pelo GlobalExceptionHandler, contra um PostgreSQL real do Testcontainers.
@CucumberContextConfiguration
@SpringBootTest(classes = IfoBackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("teste")
@Import(PostgresTestConfig.class)
public class CucumberSpringConfiguration {
}
