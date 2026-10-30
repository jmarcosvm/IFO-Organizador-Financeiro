package com.ifo.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

// Sem usuário em memória padrão: a autenticação é só por JWT
// O job diário das recorrências precisa do agendamento ligado
@EnableScheduling
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class IfoBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(IfoBackendApplication.class, args);
    }
}
