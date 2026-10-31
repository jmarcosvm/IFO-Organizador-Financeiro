// Único ponto de saída para a API (ADR-014). Nenhuma tela usa fetch direto.
// Faz três coisas por todos: coloca o token no cabeçalho, transforma qualquer erro
// do formato de 06-api.md §8 numa exceção ApiErro e leva o 401 de volta ao login.

import { lerSessao, limparSessao } from "./sessao.js";
import { toast } from "./ui.js";

// Na máquina do desenvolvedor (Live Server) a API é a local; em qualquer outro endereço
// (Vercel) é a API publicada no Render (ADR-018). O mesmo código serve os dois ambientes.
const ENDERECOS_LOCAIS = ["localhost", "127.0.0.1", "[::1]"];
const EM_PRODUCAO = !ENDERECOS_LOCAIS.includes(window.location.hostname);

export const API_URL = EM_PRODUCAO ? "https://ifo-api.onrender.com/api" : "http://localhost:8081/api";

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

const MENSAGEM_REDE = EM_PRODUCAO
  ? "Não foi possível conectar ao servidor. Se ele estava parado, aguarde cerca de 1 minuto e tente novamente."
  : "Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.";
const MENSAGEM_PADRAO = "Ocorreu um erro inesperado. Tente novamente.";

// Hospedagem gratuita (ADR-018): depois de um tempo sem uso o servidor dorme e a primeira
// resposta leva cerca de 1 minuto. Se a espera passar de alguns segundos, avisa o usuário.
// Várias requisições ao mesmo tempo (ex.: o dashboard) compartilham um único aviso.
const ESPERA_ANTES_DO_AVISO_MS = 6000;
const DURACAO_MAXIMA_DO_AVISO_MS = 120000;
const MENSAGEM_ACORDANDO = "O servidor estava em repouso e está acordando. Isso pode levar até 1 minuto, aguarde.";

let pendentes = 0;
let relogioDoAviso = null;
let avisoNaTela = null;

// Devolve a função que encerra o acompanhamento desta requisição (pode ser chamada mais de uma vez).
function avisarSeDemorar() {
  if (!EM_PRODUCAO) return () => {};

  pendentes++;
  if (pendentes === 1) {
    relogioDoAviso = setTimeout(() => {
      avisoNaTela = toast(MENSAGEM_ACORDANDO, "info", DURACAO_MAXIMA_DO_AVISO_MS);
    }, ESPERA_ANTES_DO_AVISO_MS);
  }

  let encerrada = false;
  return () => {
    if (encerrada) return;
    encerrada = true;
    pendentes--;
    if (pendentes === 0) {
      clearTimeout(relogioDoAviso);
      avisoNaTela?.remove();
      avisoNaTela = null;
    }
  };
}

// Envia e trata o erro (rede, 4xx/5xx, 401); devolve a Response quando deu certo.
// publica: true -> rota sem login (/auth/*): não envia token e o 401 NÃO derruba a sessão
async function enviar(metodo, caminho, { corpo, publica = false, aceita = "application/json" } = {}) {
  const cabecalhos = { Accept: aceita };
  if (corpo !== undefined) cabecalhos["Content-Type"] = "application/json";

  const sessao = publica ? null : lerSessao();
  if (sessao) cabecalhos.Authorization = `Bearer ${sessao.token}`;

  const encerrarAviso = avisarSeDemorar();
  let resposta;
  try {
    resposta = await fetch(`${API_URL}${caminho}`, {
      method: metodo,
      headers: cabecalhos,
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
    });
  } catch {
    encerrarAviso();
    throw new ApiErro(0, MENSAGEM_REDE);
  }

  encerrarAviso();

  if (resposta.ok) return resposta;

  const dados = await resposta.json().catch(() => null);
  const erro = new ApiErro(resposta.status, dados?.mensagem || MENSAGEM_PADRAO, dados?.campos ?? null);

  // Token ausente, inválido ou expirado: a sessão não vale mais, volta ao login
  if (resposta.status === 401 && !publica) {
    limparSessao();
    window.location.replace("index.html?sessao=expirada");
  }
  throw erro;
}

async function requisicao(metodo, caminho, opcoes) {
  const resposta = await enviar(metodo, caminho, opcoes);
  return resposta.status === 204 ? null : resposta.json();
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

// Baixa um arquivo protegido (ex.: CSV). Um link comum não serviria: o navegador
// não envia o token, então buscamos com fetch e entregamos o conteúdo como Blob.
export async function baixarArquivo(caminho, parametros) {
  const resposta = await enviar("GET", comParametros(caminho, parametros), { aceita: "*/*" });
  const blob = await resposta.blob();

  // Content-Disposition: attachment; filename="ifo-transacoes-2026-10.csv"
  const nome = /filename="?([^";]+)"?/.exec(resposta.headers.get("Content-Disposition") ?? "")?.[1] ?? "download";

  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = nome;
  document.body.append(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}
