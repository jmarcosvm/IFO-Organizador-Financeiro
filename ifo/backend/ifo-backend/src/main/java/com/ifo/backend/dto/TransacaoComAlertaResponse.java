package com.ifo.backend.dto;

// Resposta de POST/PUT /transacoes; alerta é null quando não há nada a avisar
public record TransacaoComAlertaResponse(TransacaoResponse transacao, AlertaOrcamentoResponse alerta) {}
