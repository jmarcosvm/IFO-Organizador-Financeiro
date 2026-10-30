// Formulário de despesa recorrente (criar e editar) em modal (RF20, RN14–RN17).
// Só categorias de DESPESA são oferecidas (RN14); o backend também confere.
// Nomes dos campos = nomes do DTO, para o mostrarErroForm marcar o campo certo.

import { ApiErro, post, put } from "../api.js";
import { hojeISO, nomeGrupo } from "../formato.js";
import { comBotaoOcupado, el, limparErrosForm, mostrarErroForm, numeroOuNulo, toast } from "../ui.js";
import { carregarCategorias, criarOpcoes } from "./formTransacao.js";
import { abrirModal } from "./modal.js";

const campo = (id, rotulo, entrada, ajuda) =>
  el("div", { class: "campo" }, el("label", { for: id }, rotulo), entrada, ajuda && el("span", { class: "campo-ajuda", id: `ajuda-${id}` }, ajuda));

// recorrencia: objeto da API na edição. aoSalvar recebe a RecorrenciaResponse.
export async function abrirFormRecorrencia({ recorrencia = null, aoSalvar }) {
  let categorias;
  try {
    categorias = await carregarCategorias();
  } catch (erro) {
    if (erro instanceof ApiErro && erro.status !== 401) toast(erro.mensagem, "perigo");
    return;
  }

  const porId = new Map(categorias.map((c) => [String(c.idCategoria), c]));
  const editando = recorrencia !== null;

  const selectCategoria = el("select", { id: "rec-categoria", name: "idCategoria", class: "entrada" }, criarOpcoes(categorias, { somenteDespesas: true }));
  const campoGrupo = el("input", { id: "rec-grupo", class: "entrada", type: "text", readonly: true, tabindex: "-1", "aria-describedby": "ajuda-rec-grupo" });
  const valor = el("input", { id: "rec-valor", name: "valor", class: "entrada", type: "number", inputmode: "decimal", step: "0.01", min: "0.01" });
  const descricao = el("input", { id: "rec-descricao", name: "descricao", class: "entrada", type: "text", maxlength: "255", autocomplete: "off", placeholder: "Ex.: internet, aluguel, streaming" });
  const dia = el("input", { id: "rec-dia", name: "diaMes", class: "entrada", type: "number", inputmode: "numeric", step: "1", min: "1", max: "31", "aria-describedby": "ajuda-rec-dia" });
  const inicio = el("input", { id: "rec-inicio", name: "dataInicio", class: "entrada", type: "date" });
  const fim = el("input", { id: "rec-fim", name: "dataFim", class: "entrada", type: "date", "aria-describedby": "ajuda-rec-fim" });

  const atualizarGrupo = () => {
    const categoria = porId.get(selectCategoria.value);
    campoGrupo.value = categoria ? nomeGrupo(categoria.grupo) : "";
  };
  selectCategoria.addEventListener("change", atualizarGrupo);

  selectCategoria.value = editando ? String(recorrencia.categoria.idCategoria) : "";
  valor.value = editando ? recorrencia.valor : "";
  descricao.value = editando ? (recorrencia.descricao ?? "") : "";
  dia.value = editando ? recorrencia.diaMes : "";
  inicio.value = editando ? recorrencia.dataInicio : hojeISO();
  fim.value = editando ? (recorrencia.dataFim ?? "") : "";
  atualizarGrupo();

  const cancelar = el("button", { type: "button", class: "btn btn-contorno" }, "Cancelar");
  const salvar = el("button", { type: "submit", class: "btn btn-primario" }, "Salvar");

  const form = el(
    "form",
    { class: "formulario", novalidate: true },
    editando && el("div", { class: "faixa faixa-info" }, "A alteração vale só para os meses futuros. As despesas já lançadas não mudam."),
    campo("rec-categoria", "Categoria", selectCategoria),
    campo("rec-grupo", "Grupo", campoGrupo, "Definido pela categoria."),
    campo("rec-valor", "Valor (R$)", valor),
    campo("rec-descricao", "Descrição (opcional)", descricao),
    campo("rec-dia", "Dia do mês", dia, "De 1 a 31. Em meses mais curtos, vale o último dia do mês."),
    campo("rec-inicio", "Início", inicio),
    campo("rec-fim", "Fim (opcional)", fim, "Deixe em branco para repetir sem data final."),
    el("div", { class: "modal-acoes" }, cancelar, salvar),
  );

  const modal = abrirModal({ titulo: editando ? "Editar recorrência" : "Nova despesa recorrente", conteudo: form });
  cancelar.addEventListener("click", modal.fechar);

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    limparErrosForm(form);

    const corpo = {
      idCategoria: numeroOuNulo(selectCategoria.value),
      valor: numeroOuNulo(valor.value),
      descricao: descricao.value.trim() || null,
      diaMes: numeroOuNulo(dia.value),
      dataInicio: inicio.value || null,
      dataFim: fim.value || null,
    };

    try {
      const resposta = await comBotaoOcupado(salvar, () =>
        editando ? put(`/recorrencias/${recorrencia.idRecorrencia}`, corpo) : post("/recorrencias", corpo),
      );
      modal.fechar();
      aoSalvar(resposta);
    } catch (erro) {
      mostrarErroForm(form, erro instanceof ApiErro ? erro : null);
    }
  });
}
