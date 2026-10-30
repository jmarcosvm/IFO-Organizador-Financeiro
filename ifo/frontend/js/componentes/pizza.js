// Gráfico de pizza (rosca) das despesas por grupo, sem biblioteca (RF10, ADR-006).
// Desenhado com conic-gradient e as cores dos tokens, então acompanha o tema claro/escuro.
// Acessibilidade: a cor sozinha não informa nada; a legenda traz nome, valor e % em texto
// e o desenho recebe um aria-label com o mesmo resumo.

import { el, estadoVazio } from "../ui.js";
import { moeda, nomeGrupo, percentual } from "../formato.js";

const COR_GRUPO = {
  NECESSIDADES: "var(--cor-grupo-necessidades)",
  LAZER: "var(--cor-grupo-lazer)",
  INVESTIMENTOS: "var(--cor-grupo-investimentos)",
};

// Só geometria: transforma totais em fatias de 0 a 100. Os valores exibidos vêm da API.
export function gradientePizza(itens) {
  const soma = itens.reduce((acc, i) => acc + Number(i.total), 0);
  if (soma <= 0) return null;

  let inicio = 0;
  const trechos = itens.map((item, indice) => {
    // a última fatia fecha em 100% para não sobrar fresta por arredondamento
    const fim = indice === itens.length - 1 ? 100 : inicio + (Number(item.total) / soma) * 100;
    const trecho = `${COR_GRUPO[item.grupo] ?? "var(--cor-borda)"} ${inicio.toFixed(2)}% ${fim.toFixed(2)}%`;
    inicio = fim;
    return trecho;
  });
  return `conic-gradient(${trechos.join(", ")})`;
}

export function criarPizza(despesasPorGrupo) {
  const itens = (despesasPorGrupo ?? []).filter((i) => Number(i.total) > 0);
  if (itens.length === 0) return estadoVazio("Nenhuma despesa neste mês.");

  const resumo = itens
    .map((i) => `${nomeGrupo(i.grupo)} ${percentual(i.percentual)}`)
    .join(", ");

  const desenho = el("div", {
    class: "pizza",
    role: "img",
    "aria-label": `Despesas por grupo: ${resumo}`,
  });
  desenho.style.background = gradientePizza(itens);

  const legenda = el(
    "ul",
    { class: "legenda" },
    itens.map((i) => {
      const cor = el("span", { class: "legenda-cor", "aria-hidden": "true" });
      cor.style.background = COR_GRUPO[i.grupo] ?? "var(--cor-borda)";
      return el(
        "li",
        { class: "legenda-item" },
        cor,
        el("span", {}, `${nomeGrupo(i.grupo)}: ${moeda(i.total)} (${percentual(i.percentual)})`),
      );
    }),
  );

  return el("div", { class: "pizza-bloco" }, desenho, legenda);
}
