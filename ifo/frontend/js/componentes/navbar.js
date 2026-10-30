// Barra de navegação das telas protegidas.
// Recebe os dados por parâmetro (não importa auth.js) para ser testável sozinha;
// quem monta a tela (auth.js, na Parte 2) fornece nome do usuário e a ação de sair.

import { el } from "../ui.js";
import { criarBotaoTema } from "../tema.js";

const LINKS = [
  { id: "dashboard", texto: "Dashboard", href: "dashboard.html" },
  { id: "transacoes", texto: "Transações", href: "transacoes.html" },
  { id: "orcamento", texto: "Orçamento", href: "orcamento.html" },
  { id: "metas", texto: "Metas", href: "metas.html" },
  { id: "simulador", texto: "Simulador", href: "simulador.html" },
  { id: "recorrencias", texto: "Recorrências", href: "recorrencias.html" },
  { id: "perfil", texto: "Perfil", href: "perfil.html" },
];

// ativo: id da página atual (marca o link com aria-current="page")
export function montarNavbar(destino, { ativo, nomeUsuario, aoSair }) {
  const menu = el(
    "nav",
    { id: "menu-principal", class: "navbar-menu", "aria-label": "Principal" },
    el(
      "ul",
      {},
      LINKS.map((l) =>
        el("li", {}, el("a", { href: l.href, "aria-current": l.id === ativo ? "page" : false }, l.texto)),
      ),
    ),
  );

  const alternar = el(
    "button",
    {
      type: "button",
      class: "btn btn-icone navbar-alternar",
      "aria-label": "Abrir menu",
      "aria-controls": "menu-principal",
      "aria-expanded": "false",
    },
    "☰",
  );

  const definirAberto = (aberto) => {
    menu.classList.toggle("aberto", aberto);
    alternar.setAttribute("aria-expanded", String(aberto));
    alternar.setAttribute("aria-label", aberto ? "Fechar menu" : "Abrir menu");
  };

  alternar.addEventListener("click", () => definirAberto(!menu.classList.contains("aberto")));
  // Esc fecha o menu e devolve o foco ao botão
  menu.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && menu.classList.contains("aberto")) {
      definirAberto(false);
      alternar.focus();
    }
  });

  const sair = el("button", { type: "button", class: "btn btn-contorno btn-pequeno", onclick: aoSair }, "Sair");

  const barra = el(
    "div",
    { class: "navbar-barra" },
    el("a", { class: "navbar-marca", href: "dashboard.html", "aria-label": "IFO, ir para o dashboard" }, "IFO"),
    menu,
    el("div", { class: "navbar-acoes" }, el("span", { class: "navbar-usuario" }, nomeUsuario ?? ""), criarBotaoTema(), sair, alternar),
  );

  destino.replaceChildren(barra);
}
