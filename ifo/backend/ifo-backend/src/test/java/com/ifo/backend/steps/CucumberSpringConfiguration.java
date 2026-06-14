package com.ifo.backend.steps;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import com.ifo.backend.IfoBackendApplication;

@CucumberContextConfiguration
@SpringBootTest(classes = IfoBackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CucumberSpringConfiguration {
    // Esta classe não precisa de código, só das annotations
}