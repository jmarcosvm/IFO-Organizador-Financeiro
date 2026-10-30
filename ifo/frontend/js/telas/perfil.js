import { ApiErro, get, put } from "../api.js";
import { atualizarNomeNaNavbar, iniciarTela } from "../pagina.js";
import { atualizarUsuario } from "../sessao.js";
import { comBotaoOcupado, limparErrosForm, mostrarErroForm, toast } from "../ui.js";

const usuarioSalvo = iniciarTela("perfil");
if (usuarioSalvo) iniciar();

function preencher(form, usuario) {
  form.elements.nome.value = usuario.nome;
  form.elements.email.value = usuario.email;
  form.elements.rendaMensal.value = usuario.rendaMensal;
}

async function iniciar() {
  const form = document.getElementById("form-perfil");
  const dados = document.getElementById("dados-perfil"); // <fieldset>: desabilitado até carregar

  try {
    const usuario = await get("/usuarios/me");
    preencher(form, usuario);
    dados.disabled = false;
  } catch (erro) {
    // 401 já foi tratado em api.js (volta ao login); aqui sobra falha de rede/servidor
    if (erro instanceof ApiErro && erro.status !== 401) mostrarErroForm(form, erro);
    return;
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    limparErrosForm(form);

    const botao = form.querySelector("button[type=submit]");
    try {
      const atualizado = await comBotaoOcupado(botao, () =>
        put("/usuarios/me", {
          nome: form.elements.nome.value.trim(),
          rendaMensal: form.elements.rendaMensal.value === "" ? null : Number(form.elements.rendaMensal.value),
        }),
      );
      atualizarUsuario(atualizado); // mantém a sessão guardada em dia
      atualizarNomeNaNavbar(atualizado.nome);
      preencher(form, atualizado);
      toast("Perfil atualizado.");
    } catch (erro) {
      mostrarErroForm(form, erro instanceof ApiErro ? erro : null);
    }
  });
}
