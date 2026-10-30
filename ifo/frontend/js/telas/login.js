import { ApiErro } from "../api.js";
import { entrar, redirecionarSeLogado, PAGINA_INICIAL } from "../auth.js";
import { criarBotaoTema } from "../tema.js";
import { comBotaoOcupado, el, limparErrosForm, mostrarErroForm } from "../ui.js";

if (!redirecionarSeLogado()) iniciar();

function iniciar() {
  document.body.append(criarBotaoTema("tema-flutuante"));

  const form = document.getElementById("form-login");

  // api.js manda para cá com ?sessao=expirada quando o token deixa de valer (401)
  if (new URLSearchParams(window.location.search).get("sessao") === "expirada") {
    form.before(
      el("div", { class: "faixa faixa-info", role: "status" }, "Sua sessão expirou. Entre novamente."),
    );
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    limparErrosForm(form);

    const botao = form.querySelector("button[type=submit]");
    try {
      await comBotaoOcupado(botao, () => entrar(form.elements.email.value.trim(), form.elements.senha.value));
      window.location.replace(PAGINA_INICIAL);
    } catch (erro) {
      // 400 traz `campos`; 401 traz "e-mail ou senha inválidos"; status 0 = API fora do ar
      mostrarErroForm(form, erro instanceof ApiErro ? erro : null);
    }
  });
}
