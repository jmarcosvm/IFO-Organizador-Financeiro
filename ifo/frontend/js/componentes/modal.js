// Janela modal sobre <dialog>. O navegador já prende o foco dentro dela, fecha com Esc
// e devolve o foco ao botão que a abriu. Clicar fora NÃO fecha (evita perder o que foi digitado).

import { el } from "../ui.js";

let contador = 0;

// conteudo: um nó DOM (normalmente um <form>). Devolve { fechar }.
export function abrirModal({ titulo, conteudo }) {
  const idTitulo = `modal-titulo-${++contador}`;
  const dialogo = el(
    "dialog",
    { class: "modal", "aria-labelledby": idTitulo },
    el("h2", { id: idTitulo }, titulo),
    conteudo,
  );
  dialogo.addEventListener("close", () => dialogo.remove());

  document.body.append(dialogo);
  dialogo.showModal();
  return { fechar: () => dialogo.close() };
}
