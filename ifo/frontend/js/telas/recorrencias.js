import { ApiErro, del, get, patch } from "../api.js";
import { esqueletoLista } from "../componentes/esqueletos.js";
import { abrirFormRecorrencia } from "../componentes/formRecorrencia.js";
import { dataBR, mesAno, moeda, nomeGrupo } from "../formato.js";
import { iniciarTela } from "../pagina.js";
import { confirmar, el, estadoVazio, faixaDeFalha, toast } from "../ui.js";

const usuario = iniciarTela("recorrencias");
if (usuario) iniciar();

// classe: "col-texto" (pode quebrar em qualquer ponto) ou "col-fixa" (fica inteira); ver componentes.css
function celula(rotulo, classe, ...filhos) {
  return el("td", { "data-rotulo": rotulo, class: classe }, ...filhos);
}

function linha(r, { aoEditar, aoAlternar, aoExcluir }) {
  const nome = r.descricao || r.categoria.nome; // para leitores de tela nos botões
  const botao = (texto, acao) =>
    el("button", { type: "button", class: "btn btn-contorno btn-pequeno", "aria-label": `${texto} ${nome}`, onclick: () => acao(r) }, texto);

  return el(
    "tr",
    {},
    celula("Descrição", "col-texto", r.descricao || "—"),
    celula("Categoria", "col-texto", `${r.categoria.nome} · ${nomeGrupo(r.categoria.grupo)}`),
    el("td", { class: "numero despesa", "data-rotulo": "Valor" }, `− ${moeda(r.valor)}`),
    celula("Dia", "col-fixa", String(r.diaMes)),
    celula("Período", undefined, `${dataBR(r.dataInicio)} a ${r.dataFim ? dataBR(r.dataFim) : "sem data final"}`),
    // ultimaCompetencia = último mês já lançado (RN15); vazio até a primeira geração
    celula("Lançada até", "col-fixa", mesAno(r.ultimaCompetencia)),
    celula("Situação", "col-fixa", el("span", { class: `selo ${r.ativa ? "selo-ok" : "selo-atencao"}` }, r.ativa ? "Ativa" : "Pausada")),
    celula("Ações", undefined, el("div", { class: "acoes-linha" }, botao("Editar", aoEditar), botao(r.ativa ? "Pausar" : "Reativar", aoAlternar), botao("Excluir", aoExcluir))),
  );
}

function criarTabela(recorrencias, acoes) {
  const titulos = [["Descrição"], ["Categoria"], ["Valor", "numero"], ["Dia"], ["Período"], ["Lançada até"], ["Situação"], ["Ações"]];
  return el(
    "div",
    { class: "tabela-rolagem", tabindex: "0", role: "region", "aria-label": "Tabela de despesas recorrentes" },
    el(
      "table",
      { class: "tabela tabela-cartoes" },
      el("thead", {}, el("tr", {}, titulos.map(([t, classe]) => el("th", { scope: "col", class: classe }, t)))),
      el("tbody", {}, recorrencias.map((r) => linha(r, acoes))),
    ),
  );
}

function iniciar() {
  const lista = document.getElementById("lista-recorrencias");

  async function carregar() {
    // esqueleto só na primeira carga: nas recargas (depois de editar ou pausar) a lista não pisca
    if (!lista.hasChildNodes()) lista.replaceChildren(esqueletoLista(3));
    try {
      const recorrencias = await get("/recorrencias");
      lista.replaceChildren(
        recorrencias.length === 0
          ? estadoVazio("Você ainda não tem despesas recorrentes. Cadastre aluguel, internet ou assinaturas e o IFO lança todo mês para você.")
          : criarTabela(recorrencias, { aoEditar: editar, aoAlternar: alternar, aoExcluir: excluir }),
      );
    } catch (erro) {
      if (erro instanceof ApiErro && erro.status === 401) return;
      lista.replaceChildren(faixaDeFalha(erro, carregar, "Não foi possível carregar as recorrências."));
    }
  }

  const editar = (r) =>
    abrirFormRecorrencia({
      recorrencia: r,
      aoSalvar: () => {
        toast("Recorrência atualizada para os próximos meses.");
        carregar();
      },
    });

  async function alternar(r) {
    try {
      await patch(`/recorrencias/${r.idRecorrencia}/ativa`, { ativa: !r.ativa });
      // RN16: reativar não gera os meses em que ficou pausada
      toast(r.ativa ? "Recorrência pausada." : "Recorrência reativada. Os meses em que ficou pausada não são lançados.");
      carregar();
    } catch (erro) {
      if (erro instanceof ApiErro && erro.status !== 401) toast(erro.mensagem, "perigo");
    }
  }

  async function excluir(r) {
    const confirmado = await confirmar({
      titulo: "Excluir recorrência?",
      mensagem: `“${r.descricao || r.categoria.nome}” deixará de ser lançada nos próximos meses. As despesas que ela já lançou continuam nas suas transações.`,
      textoConfirmar: "Excluir",
      perigo: true,
    });
    if (!confirmado) return;

    try {
      await del(`/recorrencias/${r.idRecorrencia}`);
      toast("Recorrência excluída.");
      carregar();
    } catch (erro) {
      if (erro instanceof ApiErro && erro.status !== 401) toast(erro.mensagem, "perigo");
    }
  }

  document.getElementById("nova-recorrencia").addEventListener("click", () =>
    abrirFormRecorrencia({
      aoSalvar: () => {
        // a API já lança as ocorrências vencidas ao criar (RN15)
        toast("Recorrência criada. As despesas já vencidas foram lançadas.");
        carregar();
      },
    }),
  );

  carregar();
}
