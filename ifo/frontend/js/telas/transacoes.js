import { ApiErro, baixarArquivo, del, get } from "../api.js";
import { mostrarAlerta } from "../componentes/alertaOrcamento.js";
import { abrirFormTransacao } from "../componentes/formTransacao.js";
import { criarSeletorMes, lerPeriodo, nomeMes } from "../componentes/seletorMes.js";
import { criarTabelaTransacoes } from "../componentes/tabelaTransacoes.js";
import { iniciarTela } from "../pagina.js";
import { comBotaoOcupado, confirmar, el, estadoVazio, toast } from "../ui.js";

const usuario = iniciarTela("transacoes");
if (usuario) iniciar();

function iniciar() {
  let periodo = lerPeriodo(window.location.search);
  let versao = 0; // descarta respostas antigas se o usuário trocar de mês rápido

  const lista = document.getElementById("lista");
  const areaAlerta = document.getElementById("area-alerta");

  async function carregar() {
    const minha = ++versao;
    lista.replaceChildren(estadoVazio("Carregando…"));

    try {
      const transacoes = await get("/transacoes", periodo);
      if (minha !== versao) return;
      lista.replaceChildren(
        criarTabelaTransacoes(transacoes, { aoEditar: editar, aoExcluir: excluir }),
      );
    } catch (erro) {
      if (minha !== versao || (erro instanceof ApiErro && erro.status === 401)) return;
      lista.replaceChildren(
        el(
          "div",
          { class: "faixa faixa-perigo", role: "alert" },
          erro instanceof ApiErro ? erro.mensagem : "Não foi possível carregar as transações.",
          " ",
          el("button", { type: "button", class: "btn btn-contorno btn-pequeno", onclick: carregar }, "Tentar novamente"),
        ),
      );
    }
  }

  // resposta = { transacao, alerta } (06-api.md §4); alerta pode ser null
  function aoSalvar({ transacao, alerta }) {
    mostrarAlerta(areaAlerta, alerta);

    const [ano, mes] = transacao.data.split("-").map(Number);
    const foraDoFiltro = mes !== periodo.mes || ano !== periodo.ano;
    toast(
      foraDoFiltro
        ? `Transação salva em ${nomeMes(mes)} de ${ano}, fora do mês filtrado.`
        : "Transação salva.",
    );
    carregar();
  }

  const editar = (transacao) => abrirFormTransacao({ transacao, aoSalvar });

  async function excluir(transacao) {
    const nome = transacao.descricao || transacao.categoria.nome;
    const confirmado = await confirmar({
      titulo: "Excluir transação?",
      mensagem: `"${nome}" será removida. Essa ação não pode ser desfeita.`,
      textoConfirmar: "Excluir",
      perigo: true,
    });
    if (!confirmado) return;

    try {
      await del(`/transacoes/${transacao.idTransacao}`);
      areaAlerta.replaceChildren(); // o aviso anterior pode não valer mais
      toast("Transação excluída.");
      carregar();
    } catch (erro) {
      if (erro instanceof ApiErro && erro.status !== 401) toast(erro.mensagem, "perigo");
    }
  }

  document.getElementById("filtro").append(
    criarSeletorMes({
      periodo,
      aoMudar: (novo) => {
        periodo = novo;
        areaAlerta.replaceChildren();
        carregar();
      },
    }),
  );

  document.getElementById("nova-transacao").addEventListener("click", () => abrirFormTransacao({ aoSalvar }));

  const botaoCsv = document.getElementById("exportar-csv");
  botaoCsv.addEventListener("click", async () => {
    try {
      await comBotaoOcupado(botaoCsv, () => baixarArquivo("/transacoes/exportar", periodo));
      toast("Download do CSV iniciado.", "info");
    } catch (erro) {
      if (erro instanceof ApiErro && erro.status !== 401) toast(erro.mensagem, "perigo");
    }
  });

  carregar();
}
