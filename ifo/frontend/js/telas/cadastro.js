import { ApiErro } from "../api.js";
import { cadastrar, redirecionarSeLogado, PAGINA_INICIAL } from "../auth.js";
import { criarBotaoTema } from "../tema.js";
import { comBotaoOcupado, limparErrosForm, mostrarErroForm } from "../ui.js";

if (!redirecionarSeLogado()) iniciar();

// Só o que o backend não conhece (confirmação de senha) ou não recebe como número
// (renda vazia vira null). O resto é validado pelo backend, que é a fonte da regra.
function validarNoCliente(form) {
  const campos = {};
  if (form.elements.senha.value !== form.elements.confirmarSenha.value) {
    campos.confirmarSenha = "as senhas não coincidem";
  }
  const renda = form.elements.rendaMensal.value;
  if (renda === "" || Number.isNaN(Number(renda))) {
    campos.rendaMensal = "informe um valor (pode ser 0)";
  }
  return campos;
}

function iniciar() {
  document.body.append(criarBotaoTema("tema-flutuante"));

  const form = document.getElementById("form-cadastro");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    limparErrosForm(form);

    const camposInvalidos = validarNoCliente(form);
    if (Object.keys(camposInvalidos).length > 0) {
      mostrarErroForm(form, { mensagem: "Verifique os campos informados.", campos: camposInvalidos });
      return;
    }

    const botao = form.querySelector("button[type=submit]");
    try {
      await comBotaoOcupado(botao, () =>
        cadastrar({
          nome: form.elements.nome.value.trim(),
          email: form.elements.email.value.trim(),
          senha: form.elements.senha.value,
          rendaMensal: Number(form.elements.rendaMensal.value),
        }),
      );
      window.location.replace(PAGINA_INICIAL); // o cadastro já devolve o token: entra direto
    } catch (erro) {
      if (erro instanceof ApiErro && erro.status === 409) {
        // e-mail repetido: marca o próprio campo
        mostrarErroForm(form, {
          mensagem: "Verifique os campos informados.",
          campos: { email: erro.mensagem },
        });
      } else {
        mostrarErroForm(form, erro instanceof ApiErro ? erro : null);
      }
    }
  });
}
