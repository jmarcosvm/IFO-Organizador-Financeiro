package com.ifo.backend.job;

import com.ifo.backend.service.RecorrenciaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

// Roda todo dia e gera as ocorrências que já venceram (RN15).
// É seguro rodar várias vezes: o gerador não duplica (RNF17).
@Component
public class RecorrenciaJob {

    private static final Logger log = LoggerFactory.getLogger(RecorrenciaJob.class);

    private final RecorrenciaService service;

    public RecorrenciaJob(RecorrenciaService service) {
        this.service = service;
    }

    @Scheduled(cron = "0 5 0 * * *")
    public void gerarOcorrenciasDoDia() {
        LocalDate hoje = LocalDate.now();
        int total = 0;
        for (Long id : service.idsAtivas()) {
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
