// Login, cadastro, logout e proteção de telas. Conversa com api.js (rede) e sessao.js (armazenamento).

import { post } from "./api.js";
import { lerSessao, salvarSessao, limparSessao } from "./sessao.js";

// Tela para onde o usuário vai depois de entrar.
export const PAGINA_INICIAL = "dashboard.html";
const PAGINA_LOGIN = "index.html";

export function usuarioAtual() {
  return lerSessao()?.usuario ?? null;
}

// Rotas /auth/* são públicas: sem token e sem derrubar a sessão em caso de 401
export async function entrar(email, senha) {
  const resposta = await post("/auth/login", { email, senha }, { publica: true });
  salvarSessao(resposta);
  return resposta.usuario;
}

// O backend já devolve o token no cadastro (201), então o usuário entra direto
export async function cadastrar({ nome, email, senha, rendaMensal }) {
  const resposta = await post("/auth/cadastro", { nome, email, senha, rendaMensal }, { publica: true });
  salvarSessao(resposta);
  return resposta.usuario;
}

export function sair() {
  limparSessao();
  window.location.replace(PAGINA_LOGIN);
}

// Chamar no topo de toda tela protegida. Sem sessão, manda para o login e devolve null:
// quem chama deve parar (`if (!usuario) return;`), pois o código seguinte ainda executaria.
export function exigirLogin() {
  const usuario = usuarioAtual();
  if (!usuario) window.location.replace(PAGINA_LOGIN);
  return usuario;
}

// Para login e cadastro: quem já está logado não precisa vê-los
export function redirecionarSeLogado() {
  if (usuarioAtual()) {
    window.location.replace(PAGINA_INICIAL);
    return true;
  }
  return false;
}
