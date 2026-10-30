// Cartão de meta financeira (RF15–RF17). progresso e concluida vêm prontos da API (RN09):
// o frontend não recalcula nada, só exibe.

import { dataBR, moeda, percentual } from "../formato.js";
import { el } from "../ui.js";
import { criarProgresso } from "./progresso.js";

// Abertas antes das concluídas; em cada bloco, prazo mais próximo primeiro; sem prazo por último
export function ordenarMetas(metas) {
  const prazo = (m) => m.prazo ?? "9999-12-31";
  return [...metas].sort((a, b) => Number(a.concluida) - Number(b.concluida) || prazo(a).localeCompare(prazo(b)));
}

export function textoPrazo(meta) {
  return meta.prazo ? `Prazo ${dataBR(meta.prazo)}` : "Sem prazo";
}

// aoAportar/aoEditar/aoExcluir recebem a meta
export function criarCartaoMeta(meta, { aoAportar, aoEditar, aoExcluir }) {
  const botao = (texto, acao, classe = "btn-contorno") =>
    el("button", { type: "button", class: `btn ${classe} btn-pequeno`, "aria-label": `${texto} ${meta.tituloMeta}`, onclick: () => acao(meta) }, texto);

  return el(
    "article",
    { class: "cartao" },
    el(
      "div",
      { class: "linha linha-entre" },
      el("h3", {}, meta.tituloMeta),
      meta.concluida && el("span", { class: "selo selo-ok" }, "Concluída"),
    ),
    el("p", { class: "valor-medio" }, moeda(meta.valorAtual)),
    criarProgresso({
      percentual: meta.progresso,
      nivel: meta.concluida ? "OK" : undefined,
      rotulo: `${meta.tituloMeta}: ${percentual(meta.progresso)} concluída`,
    }),
    el(
      "p",
      { class: "texto-suave texto-pequeno" },
      `de ${moeda(meta.valorObjetivo)} · ${percentual(meta.progresso)} · ${textoPrazo(meta)}`,
    ),
    el("div", { class: "acoes-linha acoes-inicio" }, botao("Aportar", aoAportar, "btn-primario"), botao("Editar", aoEditar), botao("Excluir", aoExcluir)),
  );
}
