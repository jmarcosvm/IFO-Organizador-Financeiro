// Esqueletos de carregamento (ADR-019): blocos cinza no lugar do conteúdo enquanto a API responde.
// O desenho é só decorativo (aria-hidden); o leitor de tela ouve "Carregando…" (role=status).
// Só montam DOM com el(): nenhum dado entra aqui, então não há o que escapar.

import { el } from "../ui.js";

function bloco(...classes) {
  return el("div", { class: ["esqueleto-bloco", ...classes].join(" ") });
}

function envelope(...desenho) {
  return el(
    "div",
    { class: "pilha", role: "status" },
    el("span", { class: "so-leitor" }, "Carregando…"),
    el("div", { class: "pilha", "aria-hidden": "true" }, ...desenho),
  );
}

// Cartão genérico: título, valor em destaque e uma linha de texto
function cartao() {
  return el("div", { class: "cartao esqueleto-pilha" }, bloco("esqueleto-titulo"), bloco("esqueleto-valor"), bloco("esqueleto-texto"));
}

// Grade de cartões (orçamento, metas)
export function esqueletoCartoes(quantidade = 3, classeGrade = "grade grade-3") {
  return envelope(el("div", { class: classeGrade }, Array.from({ length: quantidade }, cartao)));
}

// Lista ou tabela (transações, recorrências)
export function esqueletoLista(linhas = 5) {
  return envelope(...Array.from({ length: linhas }, () => bloco("esqueleto-linha")));
}

// Dashboard: três faixas de cartões, no mesmo desenho da tela pronta
export function esqueletoPainel() {
  return envelope(
    el("div", { class: "grade grade-3" }, Array.from({ length: 3 }, cartao)),
    el("div", { class: "grade grade-3" }, Array.from({ length: 3 }, cartao)),
    el("div", { class: "grade grade-2" }, cartao(), cartao()),
  );
}
