package com.ifo.backend.steps;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import com.ifo.backend.IfoBackendApplication;

// Único ponto com @SpringBootTest; as classes de steps só usam @Autowired.
// Por enquanto usa o PostgreSQL local (docker compose); Testcontainers entra na R5.
@CucumberContextConfiguration
@SpringBootTest(classes = IfoBackendApplication.class)
public class CucumberSpringConfiguration {
}
