package com.ifo.backend.service;

import com.ifo.backend.model.Transacao;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Monta o CSV das transações (RF21, RN18): ";" como separador, vírgula decimal, UTF-8 com BOM para o Excel
public final class ExportadorCsv {

    private static final String BOM = "﻿";
    private static final String SEPARADOR = ";";
    private static final String QUEBRA_DE_LINHA = "\r\n";
    private static final String CABECALHO = "Data;Tipo;Grupo;Categoria;Descrição;Valor";
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ExportadorCsv() {
    }

    public static byte[] gerar(List<Transacao> transacoes) {
        StringBuilder csv = new StringBuilder(BOM).append(CABECALHO).append(QUEBRA_DE_LINHA);
        for (Transacao t : transacoes) {
            csv.append(String.join(SEPARADOR,
                    t.getData().format(DATA),
                    t.getCategoria().getTipo().name(),
                    t.getCategoria().getGrupo() != null ? t.getCategoria().getGrupo().name() : "",
                    texto(t.getCategoria().getNome()),
                    texto(t.getDescricao()),
                    valor(t.getValor())))
                    .append(QUEBRA_DE_LINHA);
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // 1500.5 -> "1500,50": sempre 2 casas, sem separador de milhar e sem símbolo de moeda
    static String valor(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    // Texto do usuário vira célula segura: neutraliza fórmulas e protege separador, aspas e quebras de linha
    static String texto(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        String seguro = texto;
        char primeiro = seguro.charAt(0);
        // Tab e CR também iniciam fórmula em algumas planilhas (lista da OWASP: = + - @ TAB CR)
        if (primeiro == '=' || primeiro == '+' || primeiro == '-' || primeiro == '@' || primeiro == '\t' || primeiro == '\r') {
            seguro = "'" + seguro; // o Excel passa a ler como texto, não como fórmula (RNF15)
        }
        if (seguro.contains(SEPARADOR) || seguro.contains("\"") || seguro.contains("\n") || seguro.contains("\r")) {
            seguro = "\"" + seguro.replace("\"", "\"\"") + "\"";
        }
        return seguro;
    }
}
