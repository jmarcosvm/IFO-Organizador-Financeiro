import { ApiErro, get } from "../api.js";
import { criarAlertaDeStatus, mostrarAlerta, NIVEIS_DE_ALERTA } from "../componentes/alertaOrcamento.js";
import { abrirFormTransacao } from "../componentes/formTransacao.js";
import { criarCartaoGrupo } from "../componentes/cartaoGrupo.js";
import { esqueletoPainel } from "../componentes/esqueletos.js";
import { ordenarMetas, textoPrazo } from "../componentes/cartaoMeta.js";
import { criarPizza } from "../componentes/pizza.js";
import { criarProgresso } from "../componentes/progresso.js";
import { criarSeletorMes, lerPeriodo } from "../componentes/seletorMes.js";
import { criarTabelaTransacoes } from "../componentes/tabelaTransacoes.js";
import { moeda, percentual } from "../formato.js";
import { iniciarTela } from "../pagina.js";
import { el, estadoVazio, faixaDeFalha, toast } from "../ui.js";

const usuario = iniciarTela("dashboard");
if (usuario) iniciar();

function cartaoValor(titulo, valor, classe = "") {
  return el("section", { class: "cartao" }, el("h2", { class: "titulo-pequeno" }, titulo), el("p", { class: `valor-destaque ${classe}` }, moeda(valor)));
}

function cartaoMeta(metas) {
  const meta = ordenarMetas(metas)[0] ?? null; // aberta com prazo mais próximo
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
      meta.concluida ? " · Concluída" : ` · ${textoPrazo(meta)}`,
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
        el("div", { class: "grade grade-3" }, status.map(criarCartaoGrupo)),
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
    conteudo.replaceChildren(esqueletoPainel());

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
      conteudo.replaceChildren(faixaDeFalha(erro, carregar, "Não foi possível carregar o painel."));
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
