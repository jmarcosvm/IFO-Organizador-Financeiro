import { ApiErro, get } from "../api.js";
import { criarAlertaDeStatus, mostrarAlerta, NIVEIS_DE_ALERTA } from "../componentes/alertaOrcamento.js";
import { abrirFormTransacao } from "../componentes/formTransacao.js";
import { criarPizza } from "../componentes/pizza.js";
import { criarProgresso } from "../componentes/progresso.js";
import { criarSeletorMes, lerPeriodo } from "../componentes/seletorMes.js";
import { criarTabelaTransacoes } from "../componentes/tabelaTransacoes.js";
import { dataBR, moeda, nomeGrupo, nomeNivel, percentual } from "../formato.js";
import { iniciarTela } from "../pagina.js";
import { el, estadoVazio, toast } from "../ui.js";

const usuario = iniciarTela("dashboard");
if (usuario) iniciar();

function cartaoValor(titulo, valor, classe = "") {
  return el("section", { class: "cartao" }, el("h2", { class: "titulo-pequeno" }, titulo), el("p", { class: `valor-destaque ${classe}` }, moeda(valor)));
}

// Um cartão por grupo do orçamento (RF13). limite/percentualUsado vêm null sem renda (RN06)
function cartaoGrupo(status) {
  const nome = nomeGrupo(status.grupo);
  const comLimite = status.limite != null && status.percentualUsado != null;

  const filhos = [
    el(
      "div",
      { class: "linha linha-entre" },
      el("h3", {}, nome),
      comLimite && el("span", { class: `selo selo-${status.nivel.toLowerCase()}` }, nomeNivel(status.nivel)),
    ),
    el("p", { class: "valor-medio" }, moeda(status.gasto)),
  ];

  if (comLimite) {
    filhos.push(
      criarProgresso({
        percentual: status.percentualUsado,
        nivel: status.nivel,
        rotulo: `${nome}: ${percentual(status.percentualUsado)} do limite usado`,
      }),
      el(
        "p",
        { class: "texto-suave texto-pequeno" },
        `${percentual(status.percentualUsado)} de ${moeda(status.limite)} (${percentual(status.percentual)} da renda)`,
      ),
    );
  } else {
    filhos.push(el("p", { class: "texto-suave texto-pequeno" }, "Sem limite definido para este mês."));
  }
  return el("article", { class: "cartao" }, filhos);
}

// Meta ainda não concluída com o prazo mais próximo; se todas estiverem concluídas, a mais recente
function metaEmDestaque(metas) {
  const porPrazo = (a, b) => a.prazo.localeCompare(b.prazo);
  const abertas = metas.filter((m) => !m.concluida).sort(porPrazo);
  return abertas[0] ?? metas.sort(porPrazo).at(-1) ?? null;
}

function cartaoMeta(metas) {
  const meta = metaEmDestaque(metas);
  const cabecalho = el(
    "div",
    { class: "linha linha-entre" },
    el("h2", {}, "Meta em destaque"),
    el("a", { href: "metas.html", class: "btn btn-contorno btn-pequeno" }, "Ver metas"),
  );
  if (!meta) return el("section", { class: "cartao" }, cabecalho, estadoVazio("Você ainda não criou nenhuma meta."));

  return el(
    "section",
    { class: "cartao" },
    cabecalho,
    el("p", { class: "valor-medio" }, meta.tituloMeta, meta.concluida && " ✓"),
    criarProgresso({ percentual: meta.progresso, rotulo: `${meta.tituloMeta}: ${percentual(meta.progresso)} concluída` }),
    el(
      "p",
      { class: "texto-suave texto-pequeno" },
      `${moeda(meta.valorAtual)} de ${moeda(meta.valorObjetivo)} · ${percentual(meta.progresso)}`,
      meta.concluida ? " · Concluída" : ` · Prazo ${dataBR(meta.prazo)}`,
    ),
  );
}

function iniciar() {
  let periodo = lerPeriodo(window.location.search);
  let versao = 0; // descarta respostas antigas se o usuário trocar de mês rápido

  const conteudo = document.getElementById("painel");

  function renderizar({ resumo, status, transacoes, metas }) {
    const alertas = status.filter((s) => NIVEIS_DE_ALERTA.includes(s.nivel));
    const semRenda = status.length > 0 && status.every((s) => s.nivel === "SEM_LIMITE");

    conteudo.replaceChildren(
      // Alertas do mês aparecem no topo do dashboard (RF14)
      el(
        "div",
        { class: "pilha", role: "status", "aria-live": "polite" },
        alertas.map(criarAlertaDeStatus),
        semRenda &&
          el(
            "div",
            { class: "faixa faixa-info" },
            "Informe sua renda mensal no ",
            el("a", { href: "perfil.html" }, "perfil"),
            " para ver os limites do seu orçamento.",
          ),
      ),

      el(
        "div",
        { class: "grade grade-3" },
        cartaoValor("Saldo do mês", resumo.saldo, resumo.saldo < 0 ? "despesa" : ""),
        cartaoValor("Receitas", resumo.totalReceitas, "receita"),
        cartaoValor("Despesas", resumo.totalDespesas, "despesa"),
      ),

      el(
        "section",
        { "aria-labelledby": "titulo-orcamento" },
        el("h2", { id: "titulo-orcamento", class: "titulo-secao" }, "Orçamento 50/30/20"),
        el("div", { class: "grade grade-3" }, status.map(cartaoGrupo)),
      ),

      el(
        "div",
        { class: "grade grade-2" },
        el("section", { class: "cartao" }, el("h2", {}, "Despesas por grupo"), criarPizza(resumo.despesasPorGrupo)),
        cartaoMeta(metas),
      ),

      el(
        "section",
        { class: "cartao" },
        el(
          "div",
          { class: "linha linha-entre" },
          el("h2", {}, "Últimas transações"),
          el("a", { href: `transacoes.html?mes=${periodo.mes}&ano=${periodo.ano}`, class: "btn btn-contorno btn-pequeno" }, "Ver todas"),
        ),
        criarTabelaTransacoes(transacoes.slice(0, 5)),
      ),
    );
  }

  async function carregar() {
    const minha = ++versao;
    conteudo.replaceChildren(estadoVazio("Carregando…"));

    try {
      const [resumo, status, transacoes, metas] = await Promise.all([
        get("/resumo", periodo),
        get("/orcamento/status", periodo),
        get("/transacoes", periodo),
        get("/metas"),
      ]);
      if (minha !== versao) return;
      renderizar({ resumo, status, transacoes, metas });
    } catch (erro) {
      if (minha !== versao || (erro instanceof ApiErro && erro.status === 401)) return;
      conteudo.replaceChildren(
        el(
          "div",
          { class: "faixa faixa-perigo", role: "alert" },
          erro instanceof ApiErro ? erro.mensagem : "Não foi possível carregar o painel.",
          " ",
          el("button", { type: "button", class: "btn btn-contorno btn-pequeno", onclick: carregar }, "Tentar novamente"),
        ),
      );
    }
  }

  document.getElementById("filtro").append(
    criarSeletorMes({
      periodo,
      aoMudar: (novo) => {
        periodo = novo;
        carregar();
      },
    }),
  );

  // Lançar a partir do dashboard (RNF06: poucos toques). O alerta do lançamento aparece como toast
  // e o painel recarrega, trazendo a faixa atualizada do mês.
  document.getElementById("nova-transacao").addEventListener("click", () =>
    abrirFormTransacao({
      aoSalvar: ({ alerta }) => {
        toast(alerta ? alerta.mensagem : "Transação salva.", alerta ? (alerta.nivel === "ESTOURO" ? "perigo" : "atencao") : "sucesso", 8000);
        carregar();
      },
    }),
  );

  carregar();
}
