// Tema claro/escuro (RF22, RN19, ADR-013).
// Sem escolha salva, vale o prefers-color-scheme do sistema (o CSS cuida disso).
// A escolha manual vai para o localStorage e prevalece.
//
// Cada página também tem um script curto inline no <head> que aplica o tema salvo
// antes da pintura, para a tela não "piscar" no tema errado. Mantenha os dois
// em sincronia (mesma chave: "ifo.tema").

const CHAVE = "ifo.tema";
const MIDIA_ESCURO = "(prefers-color-scheme: dark)";

function lerEscolha() {
  try {
    const salvo = localStorage.getItem(CHAVE);
    return salvo === "dark" || salvo === "light" ? salvo : null;
  } catch {
    return null; // navegador com armazenamento bloqueado
  }
}

export function temaEfetivo() {
  return lerEscolha() ?? (window.matchMedia(MIDIA_ESCURO).matches ? "dark" : "light");
}

export function aplicarTema(tema) {
  document.documentElement.dataset.theme = tema;
  try {
    localStorage.setItem(CHAVE, tema);
  } catch {
    // sem armazenamento: o tema vale só nesta visita
  }
}

export function alternarTema() {
  const novo = temaEfetivo() === "dark" ? "light" : "dark";
  aplicarTema(novo);
  return novo;
}

// Botão de alternar. Quem chama decide onde ele entra na página.
export function criarBotaoTema(classeExtra = "") {
  const botao = document.createElement("button");
  botao.type = "button";
  botao.className = `btn btn-icone ${classeExtra}`.trim();

  const atualizar = () => {
    const escuro = temaEfetivo() === "dark";
    botao.textContent = escuro ? "☀" : "☾";
    botao.setAttribute("aria-label", escuro ? "Mudar para o tema claro" : "Mudar para o tema escuro");
    botao.title = botao.getAttribute("aria-label");
  };

  botao.addEventListener("click", () => {
    alternarTema();
    atualizar();
  });

  // Se o usuário nunca escolheu, acompanha o sistema em tempo real
  window.matchMedia(MIDIA_ESCURO).addEventListener("change", atualizar);

  atualizar();
  return botao;
}
