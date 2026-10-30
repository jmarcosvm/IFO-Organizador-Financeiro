import { ApiErro, del, get } from "../api.js";
import { criarCartaoMeta, ordenarMetas } from "../componentes/cartaoMeta.js";
import { abrirFormAporte, abrirFormMeta } from "../componentes/formMeta.js";
import { iniciarTela } from "../pagina.js";
import { confirmar, el, estadoVazio, faixaDeFalha, toast } from "../ui.js";

const usuario = iniciarTela("metas");
if (usuario) iniciar();

function iniciar() {
  const lista = document.getElementById("lista-metas");

  async function carregar() {
    try {
      const metas = await get("/metas");
      lista.replaceChildren(
        metas.length === 0
          ? estadoVazio("Você ainda não criou nenhuma meta. Que tal começar por uma reserva de emergência?")
          : el("div", { class: "grade grade-2" }, ordenarMetas(metas).map((m) => criarCartaoMeta(m, { aoAportar: aportar, aoEditar: editar, aoExcluir: excluir }))),
      );
    } catch (erro) {
      if (erro instanceof ApiErro && erro.status === 401) return;
      lista.replaceChildren(faixaDeFalha(erro, carregar, "Não foi possível carregar as metas."));
    }
  }

  const aportar = (meta) =>
    abrirFormAporte({
      meta,
      aoSalvar: (atualizada) => {
        // RN09: concluída quando o valor atual alcança o objetivo (aporte acima é permitido)
        toast(atualizada.concluida && !meta.concluida ? "Meta concluída! Parabéns." : "Aporte registrado.");
        carregar();
      },
    });

  const editar = (meta) =>
    abrirFormMeta({
      meta,
      aoSalvar: () => {
        toast("Meta atualizada.");
        carregar();
      },
    });

  async function excluir(meta) {
    const confirmado = await confirmar({
      titulo: "Excluir meta?",
      mensagem: `A meta “${meta.tituloMeta}” e o valor já guardado nela serão removidos. Essa ação não pode ser desfeita.`,
      textoConfirmar: "Excluir",
      perigo: true,
    });
    if (!confirmado) return;

    try {
      await del(`/metas/${meta.idMeta}`);
      toast("Meta excluída.");
      carregar();
    } catch (erro) {
      if (erro instanceof ApiErro && erro.status !== 401) toast(erro.mensagem, "perigo");
    }
  }

  document.getElementById("nova-meta").addEventListener("click", () =>
    abrirFormMeta({
      aoSalvar: () => {
        toast("Meta criada.");
        carregar();
      },
    }),
  );

  carregar();
}
