// Tabela de transações. No celular, cada linha vira um cartão (CSS .tabela-cartoes + data-rotulo).
// Sem aoEditar/aoExcluir a tabela é só leitura (usada no dashboard).
// Todo texto vem da API e entra por el() (textContent): uma descrição com <script> aparece como texto.

import { dataBR, moeda, nomeGrupo, nomeTipo } from "../formato.js";
import { el, estadoVazio } from "../ui.js";

// classe: "col-texto" (pode quebrar em qualquer ponto) ou "col-fixa" (fica inteira); ver componentes.css
function celula(rotulo, classe, ...filhos) {
  return el("td", { "data-rotulo": rotulo, class: classe }, ...filhos);
}

function linha(transacao, { aoEditar, aoExcluir }) {
  const receita = transacao.categoria.tipo === "RECEITA";
  const rotulo = transacao.descricao || transacao.categoria.nome; // para leitores de tela nos botões
  const grupo = transacao.categoria.grupo ? ` · ${nomeGrupo(transacao.categoria.grupo)}` : "";

  const celulas = [
    celula("Data", "col-fixa", dataBR(transacao.data)),
    celula("Descrição", "col-texto", transacao.descricao || "—"),
    celula("Categoria", "col-texto", `${transacao.categoria.nome}${grupo}`),
    celula("Tipo", "col-fixa", el("span", { class: `selo ${receita ? "selo-receita" : "selo-despesa"}` }, nomeTipo(transacao.categoria.tipo))),
    // o sinal em texto garante a leitura sem depender só da cor
    el("td", { class: `numero ${receita ? "receita" : "despesa"}`, "data-rotulo": "Valor" }, `${receita ? "+" : "−"} ${moeda(transacao.valor)}`),
  ];

  if (aoEditar || aoExcluir) {
    celulas.push(
      celula(
        "Ações",
        "col-fixa",
        el(
          "div",
          { class: "acoes-linha" },
          aoEditar &&
            el("button", { type: "button", class: "btn btn-contorno btn-pequeno", "aria-label": `Editar ${rotulo}`, onclick: () => aoEditar(transacao) }, "Editar"),
          aoExcluir &&
            el("button", { type: "button", class: "btn btn-contorno btn-pequeno", "aria-label": `Excluir ${rotulo}`, onclick: () => aoExcluir(transacao) }, "Excluir"),
        ),
      ),
    );
  }
  return el("tr", {}, celulas);
}

export function criarTabelaTransacoes(transacoes, opcoes = {}) {
  if (transacoes.length === 0) return estadoVazio("Nenhuma transação neste mês.");

  const comAcoes = Boolean(opcoes.aoEditar || opcoes.aoExcluir);
  const titulos = ["Data", "Descrição", "Categoria", "Tipo", "Valor", comAcoes ? "Ações" : null];

  return el(
    "div",
    { class: "tabela-rolagem", tabindex: "0", role: "region", "aria-label": "Tabela de transações" },
    el(
      "table",
      { class: "tabela tabela-cartoes" },
      el("thead", {}, el("tr", {}, titulos.filter(Boolean).map((t) => el("th", { scope: "col", class: t === "Valor" ? "numero" : false }, t)))),
      el("tbody", {}, transacoes.map((t) => linha(t, opcoes))),
    ),
  );
}
