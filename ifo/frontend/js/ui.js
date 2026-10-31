// Utilitários de interface. Regra de ouro (RNF03): dado vindo do usuário ou da API
// entra na página só por textContent / nós de texto, nunca por innerHTML.
// Por isso toda a montagem de DOM passa por el().

// el("p", { class: "texto-suave", onclick: fn }, "texto", outroNo)
//   class -> className · dataset -> data-* · on<Evento> -> addEventListener
//   true -> atributo vazio · false/null/undefined -> ignorado · filhos string viram texto
export function el(tag, props = {}, ...filhos) {
  const no = document.createElement(tag);

  for (const [chave, valor] of Object.entries(props)) {
    if (valor === false || valor == null) continue;
    if (chave === "class") no.className = valor;
    else if (chave === "dataset") Object.assign(no.dataset, valor);
    else if (chave.startsWith("on") && typeof valor === "function") {
      no.addEventListener(chave.slice(2).toLowerCase(), valor);
    } else no.setAttribute(chave, valor === true ? "" : valor);
  }

  adicionarFilhos(no, filhos);
  return no;
}

function adicionarFilhos(pai, filhos) {
  for (const filho of filhos.flat()) {
    if (filho == null || filho === false) continue;
    pai.append(filho instanceof Node ? filho : document.createTextNode(String(filho)));
  }
}

export function limpar(no) {
  no.replaceChildren();
  return no;
}

/* ---------- Toasts ---------- */

function areaDeToasts() {
  let area = document.getElementById("toasts");
  if (!area) {
    // role=status + aria-live: leitor de tela anuncia sem roubar o foco
    area = el("div", { id: "toasts", class: "toasts", role: "status", "aria-live": "polite" });
    document.body.append(area);
  }
  return area;
}

// tipo: "sucesso" | "info" | "atencao" | "perigo"
export function toast(mensagem, tipo = "sucesso", duracaoMs = 5000) {
  const aviso = el("div", { class: `faixa faixa-${tipo} toast` }, mensagem);
  areaDeToasts().append(aviso);
  setTimeout(() => aviso.remove(), duracaoMs);
  return aviso; // quem precisar tirar o aviso antes do tempo (ex.: api.js) chama aviso.remove()
}

/* ---------- Confirmação (no lugar do confirm() do navegador) ---------- */

// Devolve uma Promise<boolean>. Esc e "Cancelar" resolvem false.
export function confirmar({ titulo, mensagem, textoConfirmar = "Confirmar", perigo = false }) {
  return new Promise((resolve) => {
    const dialogo = el("dialog", { class: "modal", "aria-labelledby": "confirmar-titulo" });
    const cancelar = el("button", { type: "button", class: "btn btn-contorno" }, "Cancelar");
    const confirmarBtn = el(
      "button",
      { type: "button", class: `btn ${perigo ? "btn-perigo" : "btn-primario"}` },
      textoConfirmar,
    );

    dialogo.append(
      el("h2", { id: "confirmar-titulo" }, titulo),
      el("p", {}, mensagem),
      el("div", { class: "modal-acoes" }, cancelar, confirmarBtn),
    );

    let resultado = false;
    cancelar.addEventListener("click", () => dialogo.close());
    confirmarBtn.addEventListener("click", () => {
      resultado = true;
      dialogo.close();
    });
    dialogo.addEventListener("close", () => {
      dialogo.remove();
      resolve(resultado);
    });

    document.body.append(dialogo);
    dialogo.showModal(); // o navegador prende o foco e fecha com Esc
    cancelar.focus();
  });
}

/* ---------- Erros de formulário ---------- */

// Remove banner e mensagens de erro de uma tentativa anterior
export function limparErrosForm(form) {
  form.querySelector("[data-erro-form]")?.remove();
  form.querySelectorAll(".campo-erro").forEach((no) => no.remove());
  form.querySelectorAll("[aria-invalid]").forEach((campo) => {
    campo.removeAttribute("aria-invalid");
    // devolve a dica original (ex.: "Mínimo de 8 caracteres"), se havia
    const original = campo.dataset.descricaoOriginal;
    if (original) campo.setAttribute("aria-describedby", original);
    else campo.removeAttribute("aria-describedby");
    delete campo.dataset.descricaoOriginal;
  });
}

// Mostra um erro da API (formato de 06-api.md §8: { mensagem, campos }) no formulário.
// Cada chave de `campos` é o atributo name de um input; o resto vai para o banner.
export function mostrarErroForm(form, erro) {
  limparErrosForm(form);

  const campos = erro?.campos ?? {};
  let primeiroComErro = null;

  for (const [nome, texto] of Object.entries(campos)) {
    const campo = form.elements[nome];
    if (!campo) continue;

    const idErro = `erro-${nome}`;
    const original = campo.getAttribute("aria-describedby") ?? "";
    campo.dataset.descricaoOriginal = original;
    campo.setAttribute("aria-invalid", "true");
    // o leitor de tela lê a dica e depois o erro
    campo.setAttribute("aria-describedby", `${original} ${idErro}`.trim());
    campo.closest(".campo")?.append(el("span", { id: idErro, class: "campo-erro" }, texto));
    primeiroComErro ??= campo;
  }

  // role=alert: leitor de tela lê na hora
  const banner = el(
    "div",
    { class: "faixa faixa-perigo", role: "alert", tabindex: "-1", "data-erro-form": true },
    erro?.mensagem || "Não foi possível concluir. Tente novamente.",
  );
  form.prepend(banner);
  (primeiroComErro ?? banner).focus?.();
  if (!primeiroComErro) banner.scrollIntoView({ block: "nearest" });
}

// Campo numérico vazio vira null, para o backend responder "obrigatório" em vez de interpretar 0
export function numeroOuNulo(texto) {
  return texto === "" ? null : Number(texto);
}

/* ---------- Estados de lista ---------- */

export function estadoVazio(texto) {
  return el("p", { class: "estado-vazio" }, texto);
}

// Desabilita o botão durante a requisição (evita duplo clique = lançamento em dobro)
export async function comBotaoOcupado(botao, tarefa) {
  botao.disabled = true;
  botao.setAttribute("aria-busy", "true");
  try {
    return await tarefa();
  } finally {
    botao.disabled = false;
    botao.removeAttribute("aria-busy");
  }
}

// Faixa de erro de carregamento com botão "Tentar novamente".
// 401 não chega aqui: api.js já redirecionou ao login.
export function faixaDeFalha(erro, aoTentarDeNovo, textoPadrao = "Não foi possível carregar os dados.") {
  return el(
    "div",
    { class: "faixa faixa-perigo", role: "alert" },
    erro?.mensagem || textoPadrao,
    " ",
    el("button", { type: "button", class: "btn btn-contorno btn-pequeno", onclick: aoTentarDeNovo }, "Tentar novamente"),
  );
}
