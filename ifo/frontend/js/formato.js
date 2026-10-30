// Funções puras de formatação. Só exibe: a regra de negócio (somas, limites,
// arredondamento) fica no backend, o frontend nunca recalcula dinheiro.

const NOMES_GRUPO = {
  NECESSIDADES: "Necessidades",
  LAZER: "Lazer",
  INVESTIMENTOS: "Investimentos",
};

const NOMES_NIVEL = {
  OK: "Dentro do limite",
  ATENCAO: "Atenção",
  ESTOURO: "Estourou",
  SEM_LIMITE: "Sem limite",
};

const NOMES_TIPO = { RECEITA: "Receita", DESPESA: "Despesa" };

const formatadorMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
const formatadorPercentual = new Intl.NumberFormat("pt-BR", { maximumFractionDigits: 1 });

export function moeda(valor) {
  return formatadorMoeda.format(valor ?? 0);
}

export function percentual(valor) {
  return `${formatadorPercentual.format(valor ?? 0)}%`;
}

// "2026-10-17" -> "17/10/2026". Não passa por Date para não sofrer com fuso horário.
export function dataBR(dataISO) {
  if (!dataISO) return "-";
  const [ano, mes, dia] = dataISO.split("-");
  return `${dia}/${mes}/${ano}`;
}

export function hojeISO(agora = new Date()) {
  const mes = String(agora.getMonth() + 1).padStart(2, "0");
  const dia = String(agora.getDate()).padStart(2, "0");
  return `${agora.getFullYear()}-${mes}-${dia}`;
}

export function mesAnoAtual(agora = new Date()) {
  return { mes: agora.getMonth() + 1, ano: agora.getFullYear() };
}

export function nomeGrupo(grupo) {
  return grupo ? (NOMES_GRUPO[grupo] ?? grupo) : "—";
}

export function nomeNivel(nivel) {
  return NOMES_NIVEL[nivel] ?? nivel;
}

export function nomeTipo(tipo) {
  return NOMES_TIPO[tipo] ?? tipo;
}

// Taxa de juros: até 4 casas (a API aceita 0,0001%), ao contrário de percentual(), que arredonda em 1
const formatadorTaxa = new Intl.NumberFormat("pt-BR", { maximumFractionDigits: 4 });
export function taxa(valor) {
  return `${formatadorTaxa.format(valor ?? 0)}%`;
}
