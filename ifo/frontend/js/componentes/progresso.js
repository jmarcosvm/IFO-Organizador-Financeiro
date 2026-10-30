// Barra de progresso acessível (orçamento e metas).
// nivel (opcional): "OK" | "ATENCAO" | "ESTOURO" muda a cor; sem nível usa a cor primária.
// A barra nunca passa de 100% de largura, mas o texto ao lado mostra o valor real.

import { el } from "../ui.js";

export function criarProgresso({ percentual, nivel, rotulo }) {
  const largura = Math.max(0, Math.min(100, Number(percentual) || 0));

  const barra = el("div", { class: "progresso-barra", "data-nivel": nivel });
  barra.style.width = `${largura}%`;

  return el(
    "div",
    {
      class: "progresso",
      role: "progressbar",
      "aria-label": rotulo,
      "aria-valuemin": "0",
      "aria-valuemax": "100",
      "aria-valuenow": String(Math.round(largura)),
    },
    barra,
  );
}
