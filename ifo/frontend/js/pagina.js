// Preparo comum de toda tela protegida: exige login e monta a navbar.
// Uso, no topo de js/telas/<tela>.js:
//   const usuario = iniciarTela("perfil");
//   if (!usuario) return;   // sem sessão: já foi redirecionado ao login

import { exigirLogin, sair } from "./auth.js";
import { montarNavbar } from "./componentes/navbar.js";

// ativo: id da página no menu (ver LINKS em componentes/navbar.js)
export function iniciarTela(ativo) {
  const usuario = exigirLogin();
  if (!usuario) return null;

  montarNavbar(document.getElementById("navbar"), {
    ativo,
    nomeUsuario: usuario.nome,
    aoSair: sair,
  });
  return usuario;
}

export function atualizarNomeNaNavbar(nome) {
  const destino = document.querySelector(".navbar-usuario");
  if (destino) destino.textContent = nome;
}
