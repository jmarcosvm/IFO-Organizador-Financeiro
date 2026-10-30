package com.ifo.backend.service;

import com.ifo.backend.model.Categoria;
import com.ifo.backend.model.GrupoOrcamento;
import com.ifo.backend.model.TipoCategoria;
import com.ifo.backend.model.Transacao;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportadorCsvTest {

    private static final Categoria DELIVERY =
            new Categoria((short) 11, "Delivery", TipoCategoria.DESPESA, GrupoOrcamento.LAZER);
    private static final Categoria SALARIO = new Categoria((short) 1, "Salário", TipoCategoria.RECEITA, null);
    private static final String CABECALHO = "Data;Tipo;Grupo;Categoria;Descrição;Valor";

    @Test
    void comecaComBomUtf8() {
        byte[] bytes = ExportadorCsv.gerar(List.of());

        assertEquals((byte) 0xEF, bytes[0]);
        assertEquals((byte) 0xBB, bytes[1]);
        assertEquals((byte) 0xBF, bytes[2]);
    }

    @Test
    void semTransacoesTemSoOCabecalho() {
        assertEquals(CABECALHO + "\r\n", texto(List.of()));
    }

    // Exemplo do contrato (06-api.md §10)
    @Test
    void formataDataValorComVirgulaETipoGrupo() {
        String csv = texto(List.of(
                transacao(DELIVERY, "pizza", "55.90", LocalDate.of(2026, 10, 17)),
                transacao(SALARIO, "pagamento", "1500", LocalDate.of(2026, 10, 5))));

        assertEquals(CABECALHO + "\r\n"
                + "17/10/2026;DESPESA;LAZER;Delivery;pizza;55,90\r\n"
                + "05/10/2026;RECEITA;;Salário;pagamento;1500,00\r\n", csv);
    }

    @Test
    void valorNaoUsaSeparadorDeMilharNemNotacaoCientifica() {
        assertEquals("1234567,80", ExportadorCsv.valor(new BigDecimal("1234567.8")));
        assertEquals("0,05", ExportadorCsv.valor(new BigDecimal("0.05")));
    }

    @Test
    void neutralizaInicioQueVirariaFormula() {
        assertEquals("'=SOMA(A1)", ExportadorCsv.texto("=SOMA(A1)"));
        assertEquals("'+55119999", ExportadorCsv.texto("+55119999"));
        assertEquals("'-10", ExportadorCsv.texto("-10"));
        assertEquals("'@usuario", ExportadorCsv.texto("@usuario"));
        assertEquals("'\tcmd", ExportadorCsv.texto("\tcmd"));
        assertEquals("\"'\rcmd\"", ExportadorCsv.texto("\rcmd")); // CR também vai entre aspas
    }

    @Test
    void naoMexeEmTextoComumNemEmSinaisNoMeio() {
        assertEquals("mercado", ExportadorCsv.texto("mercado"));
        assertEquals("ida-e-volta", ExportadorCsv.texto("ida-e-volta"));
    }

    @Test
    void descricaoVaziaOuNulaViraCelulaVazia() {
        assertEquals("", ExportadorCsv.texto(null));
        assertEquals("", ExportadorCsv.texto(""));
    }

    @Test
    void textoComSeparadorAspasOuQuebraDeLinhaVaiEntreAspas() {
        assertEquals("\"pão; leite\"", ExportadorCsv.texto("pão; leite"));
        assertEquals("\"disse \"\"oi\"\"\"", ExportadorCsv.texto("disse \"oi\""));
        assertEquals("\"linha1\nlinha2\"", ExportadorCsv.texto("linha1\nlinha2"));
    }

    @Test
    void descricaoMaliciosaNaoVazaComoFormulaNoArquivo() {
        String csv = texto(List.of(transacao(DELIVERY, "=HYPERLINK(\"http://x\")", "10", LocalDate.of(2026, 10, 1))));

        assertTrue(csv.contains(";\"'=HYPERLINK(\"\"http://x\"\")\";10,00"));
    }

    private String texto(List<Transacao> transacoes) {
        // O BOM vira o caractere ﻿ ao decodificar; tiramos para comparar só o conteúdo
        return new String(ExportadorCsv.gerar(transacoes), StandardCharsets.UTF_8).substring(1);
    }

    private Transacao transacao(Categoria categoria, String descricao, String valor, LocalDate data) {
        Transacao t = new Transacao();
        t.setCategoria(categoria);
        t.setDescricao(descricao);
        t.setValor(new BigDecimal(valor));
        t.setData(data);
        return t;
    }
}
