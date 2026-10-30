import { ApiErro, get, put } from "../api.js";
import { criarCartaoGrupo } from "../componentes/cartaoGrupo.js";
import { descreverSoma, somaEmCentesimos, TOTAL_CENTESIMOS } from "../componentes/percentuais.js";
import { criarSeletorMes, lerPeriodo } from "../componentes/seletorMes.js";
import { moeda } from "../formato.js";
import { iniciarTela } from "../pagina.js";
import { comBotaoOcupado, el, estadoVazio, faixaDeFalha, limparErrosForm, mostrarErroForm, numeroOuNulo, toast } from "../ui.js";

// `campo` = name do input = chave que o backend usa nos erros de validação (nome do campo no DTO)
const GRUPOS = [
  { chave: "NECESSIDADES", campo: "necessidades" },
  { chave: "LAZER", campo: "lazer" },
  { chave: "INVESTIMENTOS", campo: "investimentos" },
];
const PADRAO_50_30_20 = { necessidades: 50, lazer: 30, investimentos: 20 };

const usuario = iniciarTela("orcamento");
if (usuario) iniciar();

function iniciar() {
  let periodo = lerPeriodo(window.location.search);
  let versao = 0; // descarta respostas antigas se o usuário trocar de mês rápido

  const form = document.getElementById("form-orcamento");
  const soma = document.getElementById("soma-percentuais");
  const infoRenda = document.getElementById("info-renda");
  const status = document.getElementById("status-grupos");
  const entradas = GRUPOS.map((g) => form.elements[g.campo]);

  function atualizarSoma() {
    const { fecha, texto } = descreverSoma(somaEmCentesimos(entradas.map((e) => e.value)));
    soma.className = `faixa ${fecha ? "faixa-sucesso" : "faixa-atencao"}`;
    soma.textContent = texto;
  }
  entradas.forEach((entrada) => entrada.addEventListener("input", atualizarSoma));

  // lista = [{ grupo, percentual }] (06-api.md §5)
  function preencher(lista) {
    for (const g of GRUPOS) form.elements[g.campo].value = lista.find((o) => o.grupo === g.chave)?.percentual ?? "";
    atualizarSoma();
  }

  async function carregarFormulario() {
    try {
      const [orcamento, eu] = await Promise.all([get("/orcamento"), get("/usuarios/me")]);
      preencher(orcamento);
      infoRenda.replaceChildren(
        Number(eu.rendaMensal) > 0
          ? el("p", { class: "texto-suave" }, `Renda mensal considerada: ${moeda(eu.rendaMensal)}. O limite de cada grupo é a renda vezes o percentual. `, el("a", { href: "perfil.html" }, "Alterar renda"))
          : el("div", { class: "faixa faixa-info" }, "Sua renda mensal está zerada, então não há limites para calcular. ", el("a", { href: "perfil.html" }, "Informe sua renda no perfil"), "."),
      );
    } catch (erro) {
      if (erro instanceof ApiErro && erro.status === 401) return;
      infoRenda.replaceChildren(faixaDeFalha(erro, carregarFormulario, "Não foi possível carregar o orçamento."));
    }
  }

  async function carregarStatus() {
    const minha = ++versao;
    status.replaceChildren(estadoVazio("Carregando…"));
    try {
      const lista = await get("/orcamento/status", periodo);
      if (minha !== versao) return;
      status.replaceChildren(...lista.map(criarCartaoGrupo));
    } catch (erro) {
      if (minha !== versao || (erro instanceof ApiErro && erro.status === 401)) return;
      status.replaceChildren(faixaDeFalha(erro, carregarStatus, "Não foi possível carregar o uso do orçamento."));
    }
  }

  document.getElementById("restaurar-padrao").addEventListener("click", () => {
    limparErrosForm(form);
    for (const g of GRUPOS) form.elements[g.campo].value = PADRAO_50_30_20[g.campo];
    atualizarSoma();
  });

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    limparErrosForm(form);

    const total = somaEmCentesimos(entradas.map((x) => x.value));
    if (total !== TOTAL_CENTESIMOS) {
      // o backend também recusa (RN05); avisar antes poupa a viagem
      mostrarErroForm(form, { mensagem: `A soma dos percentuais deve ser 100%. ${descreverSoma(total).texto}` });
      return;
    }

    const botao = form.querySelector("button[type=submit]");
    try {
      const lista = await comBotaoOcupado(botao, () =>
        put("/orcamento", {
          NECESSIDADES: numeroOuNulo(form.elements.necessidades.value),
          LAZER: numeroOuNulo(form.elements.lazer.value),
          INVESTIMENTOS: numeroOuNulo(form.elements.investimentos.value),
        }),
      );
      preencher(lista);
      toast("Orçamento atualizado.");
      carregarStatus(); // os limites mudaram
    } catch (erro) {
      mostrarErroForm(form, erro instanceof ApiErro ? erro : null);
    }
  });

  document.getElementById("filtro").append(
    criarSeletorMes({
      periodo,
      aoMudar: (novo) => {
        periodo = novo;
        carregarStatus();
      },
    }),
  );

  carregarFormulario();
  carregarStatus();
}
