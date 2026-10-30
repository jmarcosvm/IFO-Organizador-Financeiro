// Formulários de meta em modal: criar/editar a meta e registrar um aporte.
// Nomes dos campos = nomes do DTO (tituloMeta, valorObjetivo, prazo, valor), para o
// mostrarErroForm marcar o campo certo quando a API devolve `campos`.

import { ApiErro, post, put } from "../api.js";
import { moeda } from "../formato.js";
import { comBotaoOcupado, el, limparErrosForm, mostrarErroForm, numeroOuNulo } from "../ui.js";
import { abrirModal } from "./modal.js";

function montarFormulario({ titulo, campos, textoSalvar, enviar, aoSalvar }) {
  const cancelar = el("button", { type: "button", class: "btn btn-contorno" }, "Cancelar");
  const salvar = el("button", { type: "submit", class: "btn btn-primario" }, textoSalvar);
  const form = el("form", { class: "formulario", novalidate: true }, campos, el("div", { class: "modal-acoes" }, cancelar, salvar));

  const modal = abrirModal({ titulo, conteudo: form });
  cancelar.addEventListener("click", modal.fechar);

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    limparErrosForm(form);
    try {
      const resposta = await comBotaoOcupado(salvar, () => enviar(form));
      modal.fechar();
      aoSalvar(resposta);
    } catch (erro) {
      mostrarErroForm(form, erro instanceof ApiErro ? erro : null);
    }
  });
}

const campo = (id, rotulo, entrada, ajuda) =>
  el("div", { class: "campo" }, el("label", { for: id }, rotulo), entrada, ajuda && el("span", { class: "campo-ajuda", id: `ajuda-${id}` }, ajuda));

// meta: objeto da API na edição; aoSalvar recebe a MetaResponse
export function abrirFormMeta({ meta = null, aoSalvar }) {
  const titulo = el("input", { id: "meta-titulo", name: "tituloMeta", class: "entrada", type: "text", maxlength: "100", autocomplete: "off", placeholder: "Ex.: Notebook, Reserva de emergência" });
  const objetivo = el("input", { id: "meta-objetivo", name: "valorObjetivo", class: "entrada", type: "number", inputmode: "decimal", step: "0.01", min: "0.01" });
  const prazo = el("input", { id: "meta-prazo", name: "prazo", class: "entrada", type: "date", "aria-describedby": "ajuda-meta-prazo" });

  titulo.value = meta?.tituloMeta ?? "";
  objetivo.value = meta?.valorObjetivo ?? "";
  prazo.value = meta?.prazo ?? "";

  montarFormulario({
    titulo: meta ? "Editar meta" : "Nova meta",
    textoSalvar: "Salvar",
    campos: [
      campo("meta-titulo", "Título", titulo),
      campo("meta-objetivo", "Valor objetivo (R$)", objetivo),
      campo("meta-prazo", "Prazo (opcional)", prazo, "Deixe em branco se a meta não tem data."),
    ],
    enviar: (form) => {
      const corpo = {
        tituloMeta: titulo.value.trim(),
        valorObjetivo: numeroOuNulo(objetivo.value),
        prazo: prazo.value || null,
      };
      return meta ? put(`/metas/${meta.idMeta}`, corpo) : post("/metas", corpo);
    },
    aoSalvar,
  });
}

export function abrirFormAporte({ meta, aoSalvar }) {
  const valor = el("input", { id: "aporte-valor", name: "valor", class: "entrada", type: "number", inputmode: "decimal", step: "0.01", min: "0.01", "aria-describedby": "ajuda-aporte-valor" });

  montarFormulario({
    titulo: `Aportar em “${meta.tituloMeta}”`,
    textoSalvar: "Registrar aporte",
    campos: [
      campo("aporte-valor", "Valor do aporte (R$)", valor, `Já guardado: ${moeda(meta.valorAtual)}. Aportar acima do objetivo é permitido.`),
    ],
    enviar: () => post(`/metas/${meta.idMeta}/aportes`, { valor: numeroOuNulo(valor.value) }),
    aoSalvar,
  });
}
