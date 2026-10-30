// Único ponto de saída para a API (ADR-014). Nenhuma tela usa fetch direto.
// Faz três coisas por todos: coloca o token no cabeçalho, transforma qualquer erro
// do formato de 06-api.md §8 numa exceção ApiErro e leva o 401 de volta ao login.

import { lerSessao, limparSessao } from "./sessao.js";

export const API_URL = "http://localhost:8081/api";

export class ApiErro extends Error {
  // status 0 = nem chegou ao servidor (API fora do ar, sem rede, CORS)
  constructor(status, mensagem, campos = null) {
    super(mensagem);
    this.name = "ApiErro";
    this.status = status;
    this.mensagem = mensagem;
    this.campos = campos; // { nomeDoCampo: "mensagem" } quando é erro de validação
  }
}

const MENSAGEM_REDE = "Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.";
const MENSAGEM_PADRAO = "Ocorreu um erro inesperado. Tente novamente.";

// publica: true -> rota sem login (/auth/*): não envia token e o 401 NÃO derruba a sessão
async function requisicao(metodo, caminho, { corpo, publica = false } = {}) {
  const cabecalhos = { Accept: "application/json" };
  if (corpo !== undefined) cabecalhos["Content-Type"] = "application/json";

  const sessao = publica ? null : lerSessao();
  if (sessao) cabecalhos.Authorization = `Bearer ${sessao.token}`;

  let resposta;
  try {
    resposta = await fetch(`${API_URL}${caminho}`, {
      method: metodo,
      headers: cabecalhos,
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
    });
  } catch {
    throw new ApiErro(0, MENSAGEM_REDE);
  }

  if (resposta.ok) {
    return resposta.status === 204 ? null : resposta.json();
  }

  const dados = await resposta.json().catch(() => null);
  const erro = new ApiErro(resposta.status, dados?.mensagem || MENSAGEM_PADRAO, dados?.campos ?? null);

  // Token ausente, inválido ou expirado: a sessão não vale mais, volta ao login
  if (resposta.status === 401 && !publica) {
    limparSessao();
    window.location.replace("index.html?sessao=expirada");
  }
  throw erro;
}

// Monta "?mes=10&ano=2026" ignorando o que for vazio
function comParametros(caminho, parametros) {
  const busca = new URLSearchParams();
  for (const [chave, valor] of Object.entries(parametros ?? {})) {
    if (valor !== undefined && valor !== null && valor !== "") busca.set(chave, valor);
  }
  const texto = busca.toString();
  return texto ? `${caminho}?${texto}` : caminho;
}

export const get = (caminho, parametros, opcoes) =>
  requisicao("GET", comParametros(caminho, parametros), opcoes);
export const post = (caminho, corpo, opcoes) => requisicao("POST", caminho, { corpo, ...opcoes });
export const put = (caminho, corpo, opcoes) => requisicao("PUT", caminho, { corpo, ...opcoes });
export const patch = (caminho, corpo, opcoes) => requisicao("PATCH", caminho, { corpo, ...opcoes });
export const del = (caminho, opcoes) => requisicao("DELETE", caminho, opcoes);
