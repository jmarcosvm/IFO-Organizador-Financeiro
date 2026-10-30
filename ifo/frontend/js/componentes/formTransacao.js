// Formulário de transação (criar e editar), usado no dashboard e na tela de transações.
// Fluxo: categoria -> grupo exibido automaticamente -> descrição (RN02, ADR-002).
// O cliente envia só { idCategoria, valor, data, descricao }: tipo e grupo a API deriva da categoria.

import { ApiErro, get, post, put } from "../api.js";
import { hojeISO, nomeGrupo } from "../formato.js";
import { comBotaoOcupado, el, limparErrosForm, mostrarErroForm, numeroOuNulo, toast } from "../ui.js";
import { abrirModal } from "./modal.js";

const ORDEM_GRUPOS = ["NECESSIDADES", "LAZER", "INVESTIMENTOS"];

// As categorias são fixas (ADR-001): busca uma vez por página e reaproveita
let categoriasEmCache = null;
export function carregarCategorias() {
  categoriasEmCache ??= get("/categorias").catch((erro) => {
    categoriasEmCache = null; // permite tentar de novo depois de uma falha
    throw erro;
  });
  return categoriasEmCache;
}

// <optgroup> por tipo/grupo: Receitas, e as despesas separadas por grupo
function criarOpcoes(categorias) {
  const opcao = (c) => el("option", { value: String(c.idCategoria) }, c.nome);
  const receitas = categorias.filter((c) => c.tipo === "RECEITA");
  const grupos = ORDEM_GRUPOS.map((grupo) => ({
    grupo,
    categorias: categorias.filter((c) => c.tipo === "DESPESA" && c.grupo === grupo),
  }));

  return [
    el("option", { value: "" }, "Selecione…"),
    el("optgroup", { label: "Receitas" }, receitas.map(opcao)),
    ...grupos.map((g) => el("optgroup", { label: `Despesas · ${nomeGrupo(g.grupo)}` }, g.categorias.map(opcao))),
  ];
}

// transacao: objeto da API quando for edição. aoSalvar recebe { transacao, alerta }.
export async function abrirFormTransacao({ transacao = null, aoSalvar }) {
  let categorias;
  try {
    categorias = await carregarCategorias();
  } catch (erro) {
    if (erro instanceof ApiErro && erro.status !== 401) toast(erro.mensagem, "perigo");
    return;
  }

  const porId = new Map(categorias.map((c) => [String(c.idCategoria), c]));
  const editando = transacao !== null;

  const selectCategoria = el("select", { id: "tr-categoria", name: "idCategoria", class: "entrada", required: true }, criarOpcoes(categorias));
  const campoGrupo = el("input", {
    id: "tr-grupo",
    class: "entrada",
    type: "text",
    readonly: true,
    tabindex: "-1",
    "aria-describedby": "ajuda-grupo",
  });
  const campoValor = el("input", {
    id: "tr-valor",
    name: "valor",
    class: "entrada",
    type: "number",
    inputmode: "decimal",
    step: "0.01",
    min: "0.01",
    required: true,
  });
  const campoData = el("input", { id: "tr-data", name: "data", class: "entrada", type: "date", required: true });
  const campoDescricao = el("input", {
    id: "tr-descricao",
    name: "descricao",
    class: "entrada",
    type: "text",
    maxlength: "255",
    autocomplete: "off",
    placeholder: "Ex.: pizza, internet, mercado",
  });

  // Mostra o grupo da categoria escolhida, sem o usuário digitar nada
  const atualizarGrupo = () => {
    const categoria = porId.get(selectCategoria.value);
    if (!categoria) campoGrupo.value = "";
    else campoGrupo.value = categoria.tipo === "RECEITA" ? "Receita (sem grupo de orçamento)" : nomeGrupo(categoria.grupo);
  };
  selectCategoria.addEventListener("change", atualizarGrupo);

  // Valores iniciais (value é propriedade, não atributo, para refletir no campo)
  selectCategoria.value = editando ? String(transacao.categoria.idCategoria) : "";
  campoValor.value = editando ? transacao.valor : "";
  campoData.value = editando ? transacao.data : hojeISO();
  campoDescricao.value = editando ? (transacao.descricao ?? "") : "";
  atualizarGrupo();

  const cancelar = el("button", { type: "button", class: "btn btn-contorno" }, "Cancelar");
  const salvar = el("button", { type: "submit", class: "btn btn-primario" }, "Salvar");

  const form = el(
    "form",
    { class: "formulario", novalidate: true },
    el("div", { class: "campo" }, el("label", { for: "tr-categoria" }, "Categoria"), selectCategoria),
    el(
      "div",
      { class: "campo" },
      el("label", { for: "tr-grupo" }, "Grupo"),
      campoGrupo,
      el("span", { id: "ajuda-grupo", class: "campo-ajuda" }, "Definido pela categoria."),
    ),
    el("div", { class: "campo" }, el("label", { for: "tr-valor" }, "Valor (R$)"), campoValor),
    el("div", { class: "campo" }, el("label", { for: "tr-data" }, "Data"), campoData),
    el("div", { class: "campo" }, el("label", { for: "tr-descricao" }, "Descrição (opcional)"), campoDescricao),
    el("div", { class: "modal-acoes" }, cancelar, salvar),
  );

  const modal = abrirModal({ titulo: editando ? "Editar transação" : "Nova transação", conteudo: form });
  cancelar.addEventListener("click", modal.fechar);

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    limparErrosForm(form);

    const corpo = {
      idCategoria: numeroOuNulo(selectCategoria.value),
      valor: numeroOuNulo(campoValor.value),
      data: campoData.value || null,
      descricao: campoDescricao.value.trim() || null,
    };

    try {
      const resposta = await comBotaoOcupado(salvar, () =>
        editando ? put(`/transacoes/${transacao.idTransacao}`, corpo) : post("/transacoes", corpo),
      );
      modal.fechar();
      aoSalvar(resposta); // { transacao, alerta }
    } catch (erro) {
      mostrarErroForm(form, erro instanceof ApiErro ? erro : null);
    }
  });
}
