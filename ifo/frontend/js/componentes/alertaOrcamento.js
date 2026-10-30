// Faixa de alerta de orçamento (RF14): amarela em ATENCAO, vermelha em ESTOURO.
// É só um aviso: nunca bloqueia nada (RN08). O texto diz o nível, então a cor não é a única pista.

import { el } from "../ui.js";
import { moeda, nomeGrupo, nomeNivel, percentual } from "../formato.js";

// `alerta` tem o formato de 06-api.md §4: { grupo, nivel, percentualUsado, limite, gasto, mensagem }
export function criarAlerta(alerta) {
  const estouro = alerta.nivel === "ESTOURO";
  return el(
    "div",
    { class: `faixa ${estouro ? "faixa-perigo" : "faixa-atencao"}` },
    el("strong", {}, `${nomeNivel(alerta.nivel)} · ${nomeGrupo(alerta.grupo)}: `),
    alerta.mensagem,
    el(
      "div",
      { class: "texto-pequeno" },
      `Gasto de ${moeda(alerta.gasto)} para um limite de ${moeda(alerta.limite)} (${percentual(alerta.percentualUsado)}).`,
    ),
  );
}

// O status do orçamento (/orcamento/status) não traz mensagem pronta; monta a mesma faixa
export function criarAlertaDeStatus(status) {
  const grupo = nomeGrupo(status.grupo);
  return criarAlerta({
    ...status,
    mensagem:
      status.nivel === "ESTOURO"
        ? `Você ultrapassou o limite de ${grupo} deste mês.`
        : `Você já usou ${percentual(status.percentualUsado)} do limite de ${grupo} deste mês.`,
  });
}

export const NIVEIS_DE_ALERTA = ["ATENCAO", "ESTOURO"];

// Troca o conteúdo de uma região aria-live pelo alerta (ou esvazia, se não houver)
export function mostrarAlerta(destino, alerta) {
  destino.replaceChildren();
  if (alerta) destino.append(criarAlerta(alerta));
}
