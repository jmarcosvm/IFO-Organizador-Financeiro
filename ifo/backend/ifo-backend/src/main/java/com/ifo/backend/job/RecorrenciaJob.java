package com.ifo.backend.job;

import com.ifo.backend.service.RecorrenciaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

// Roda todo dia e gera as ocorrências que já venceram (RN15).
// Também roda ao subir a aplicação: na hospedagem gratuita o servidor dorme e pode perder o
// horário do cron (ADR-018). É seguro rodar várias vezes: o gerador não duplica (RNF17).
@Component
public class RecorrenciaJob {

    private static final Logger log = LoggerFactory.getLogger(RecorrenciaJob.class);

    private final RecorrenciaService service;

    public RecorrenciaJob(RecorrenciaService service) {
        this.service = service;
    }

    @Scheduled(cron = "0 5 0 * * *")
    public void gerarOcorrenciasDoDia() {
        gerar(LocalDate.now());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void gerarAoSubir() {
        try {
            gerar(LocalDate.now());
        } catch (RuntimeException e) {
            // Falha aqui (ex.: banco ainda acordando) não pode derrubar a subida da API
            log.error("Falha ao gerar as recorrências na subida da aplicação", e);
        }
    }

    // "hoje" entra por parâmetro para o teste fixar a data
    void gerar(LocalDate hoje) {
        List<Long> ids = service.idsAtivas();
        int total = 0;
        for (Long id : ids) {
            try {
                total += service.gerarPorId(id, hoje);
            } catch (RuntimeException e) {
                // Uma recorrência com problema não impede as outras
                log.error("Falha ao gerar a recorrência {}", id, e);
            }
        }
        log.info("Recorrências: {} transação(ões) gerada(s) em {}", total, hoje);
    }
}
