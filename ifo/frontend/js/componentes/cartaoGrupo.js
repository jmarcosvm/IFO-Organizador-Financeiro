// Cartão de um grupo do orçamento (RF13): gasto, limite, % usado e nível.
// Usado no dashboard e na tela de orçamento. `status` é o item de GET /orcamento/status.
// limite/percentualUsado vêm null sem renda (RN06): nesse caso não há barra.

import { moeda, nomeGrupo, nomeNivel, percentual } from "../formato.js";
import { el } from "../ui.js";
import { criarProgresso } from "./progresso.js";

export function criarCartaoGrupo(status) {
  const nome = nomeGrupo(status.grupo);
  const comLimite = status.limite != null && status.percentualUsado != null;

  const filhos = [
    el(
      "div",
      { class: "linha linha-entre" },
      el("h3", {}, nome),
      comLimite && el("span", { class: `selo selo-${status.nivel.toLowerCase()}` }, nomeNivel(status.nivel)),
    ),
    el("p", { class: "valor-medio" }, moeda(status.gasto)),
  ];

  if (comLimite) {
    filhos.push(
      criarProgresso({
        percentual: status.percentualUsado,
        nivel: status.nivel,
        rotulo: `${nome}: ${percentual(status.percentualUsado)} do limite usado`,
      }),
      el(
        "p",
        { class: "texto-suave texto-pequeno" },
        `${percentual(status.percentualUsado)} de ${moeda(status.limite)} (${percentual(status.percentual)} da renda)`,
      ),
    );
  } else {
    filhos.push(el("p", { class: "texto-suave texto-pequeno" }, "Sem limite definido para este mês."));
  }
  return el("article", { class: "cartao" }, filhos);
}
