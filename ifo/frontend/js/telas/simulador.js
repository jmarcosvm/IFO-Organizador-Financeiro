import { ApiErro, post } from "../api.js";
import { moeda, taxa } from "../formato.js";
import { iniciarTela } from "../pagina.js";
import { comBotaoOcupado, el, limparErrosForm, mostrarErroForm, numeroOuNulo } from "../ui.js";

const usuario = iniciarTela("simulador");
if (usuario) iniciar();

function cartaoValor(titulo, valor, destaque = false) {
  return el("div", { class: "cartao" }, el("h3", { class: "titulo-pequeno" }, titulo), el("p", { class: destaque ? "valor-destaque" : "valor-medio" }, moeda(valor)));
}

// Evolução mês a mês (RF19). Recolhida quando é longa, para não alongar a página
function criarEvolucao(evolucao) {
  const tabela = el(
    "div",
    { class: "tabela-rolagem" },
    el(
      "table",
      { class: "tabela tabela-cartoes" },
      el("thead", {}, el("tr", {}, el("th", { scope: "col" }, "Mês"), el("th", { scope: "col", class: "numero" }, "Total investido"), el("th", { scope: "col", class: "numero" }, "Montante"))),
      el(
        "tbody",
        {},
        evolucao.map((linha) =>
          el("tr", {}, el("td", { "data-rotulo": "Mês" }, String(linha.mes)), el("td", { class: "numero", "data-rotulo": "Total investido" }, moeda(linha.investido)), el("td", { class: "numero", "data-rotulo": "Montante" }, moeda(linha.montante))),
        ),
      ),
    ),
  );
  return el("details", { class: "cartao evolucao", open: evolucao.length <= 24 }, el("summary", {}, `Evolução mês a mês (${evolucao.length} ${evolucao.length === 1 ? "mês" : "meses"})`), tabela);
}

function iniciar() {
  const form = document.getElementById("form-simulador");
  const resultado = document.getElementById("resultado");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    limparErrosForm(form);

    // Os nomes dos campos são os do DTO; a regra (RN13) e as validações ficam no backend (ADR-003)
    const corpo = {
      aplicacaoInicial: numeroOuNulo(form.elements.aplicacaoInicial.value), // vazio = 0 no backend
      valorMensal: numeroOuNulo(form.elements.valorMensal.value),
      taxaMensal: numeroOuNulo(form.elements.taxaMensal.value),
      tempoMeses: numeroOuNulo(form.elements.tempoMeses.value),
      tipoJuros: form.elements.tipoJuros.value,
    };

    const botao = form.querySelector("button[type=submit]");
    try {
      const resposta = await comBotaoOcupado(botao, () => post("/investimentos/simular", corpo));

      const titulo = el("h2", { tabindex: "-1" }, "Resultado");
      const tipo = corpo.tipoJuros === "COMPOSTO" ? "juros compostos" : "juros simples";
      resultado.replaceChildren(
        titulo,
        el(
          "p",
          { class: "texto-suave" },
          `${moeda(corpo.aplicacaoInicial ?? 0)} de aplicação inicial e ${moeda(corpo.valorMensal)} por mês, durante ${corpo.tempoMeses} ${corpo.tempoMeses === 1 ? "mês" : "meses"}, a ${taxa(corpo.taxaMensal)} ao mês (${tipo}). O aporte é feito no fim de cada mês.`,
        ),
        el("div", { class: "grade grade-3" }, cartaoValor("Montante final", resposta.montanteFinal, true), cartaoValor("Total investido", resposta.totalInvestido), cartaoValor("Rendimento", resposta.rendimento)),
        criarEvolucao(resposta.evolucao),
      );
      titulo.focus(); // leitor de tela anuncia o resultado; o foco também leva a tela até ele
    } catch (erro) {
      mostrarErroForm(form, erro instanceof ApiErro ? erro : null);
    }
  });
}
