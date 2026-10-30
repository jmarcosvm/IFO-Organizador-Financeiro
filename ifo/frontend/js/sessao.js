// Guarda a sessão (token JWT + dados do usuário) no localStorage (ADR-014).
// Fica separado de api.js e auth.js porque os dois precisam dela: se um importasse
// o outro para isso, os módulos ficariam dependendo um do outro em círculo.

const CHAVE = "ifo.sessao";
const CHAVE_V1 = "usuarioLogado"; // sessão da V1 (sem token); descartada por segurança

export function lerSessao() {
  try {
    const sessao = JSON.parse(localStorage.getItem(CHAVE));
    return sessao?.token && sessao?.usuario ? sessao : null;
  } catch {
    return null; // vazio, corrompido ou armazenamento bloqueado
  }
}

export function salvarSessao({ token, usuario }) {
  localStorage.setItem(CHAVE, JSON.stringify({ token, usuario }));
  localStorage.removeItem(CHAVE_V1);
}

// Atualiza só os dados do usuário (ex.: depois de editar o perfil), mantendo o token
export function atualizarUsuario(usuario) {
  const sessao = lerSessao();
  if (sessao) salvarSessao({ token: sessao.token, usuario });
}

export function limparSessao() {
  try {
    localStorage.removeItem(CHAVE);
    localStorage.removeItem(CHAVE_V1);
  } catch {
    // sem armazenamento, não há o que limpar
  }
}
