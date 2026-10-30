// Seletor de mês/ano usado no dashboard e nas transações.
// O período vai na URL (?mes=10&ano=2026): recarregar a página ou copiar o link mantém o filtro.

import { el } from "../ui.js";
import { mesAnoAtual } from "../formato.js";

const MESES = [
  "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
  "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro",
];

// Lê ?mes=&ano= da URL; valor ausente ou absurdo cai no mês atual
export function lerPeriodo(busca, agora = new Date()) {
  const padrao = mesAnoAtual(agora);
  const params = new URLSearchParams(busca);
  const mes = Number(params.get("mes"));
  const ano = Number(params.get("ano"));
  const mesOk = Number.isInteger(mes) && mes >= 1 && mes <= 12;
  const anoOk = Number.isInteger(ano) && ano >= 2000 && ano <= 2100;
  return mesOk && anoOk ? { mes, ano } : padrao;
}

export function nomeMes(mes) {
  return MESES[mes - 1];
}

// aoMudar({ mes, ano }) é chamado a cada mudança
export function criarSeletorMes({ periodo, aoMudar, agora = new Date() }) {
  const anoAtual = agora.getFullYear();
  const primeiro = Math.min(anoAtual - 5, periodo.ano);
  const ultimo = Math.max(anoAtual + 1, periodo.ano);

  const selectMes = el(
    "select",
    { id: "filtro-mes", class: "entrada entrada-auto" },
    MESES.map((nome, i) => el("option", { value: String(i + 1) }, nome)),
  );
  const anos = [];
  for (let ano = ultimo; ano >= primeiro; ano--) anos.push(el("option", { value: String(ano) }, String(ano)));
  const selectAno = el("select", { id: "filtro-ano", class: "entrada entrada-auto" }, anos);

  selectMes.value = String(periodo.mes);
  selectAno.value = String(periodo.ano);

  const mudou = () => {
    const novo = { mes: Number(selectMes.value), ano: Number(selectAno.value) };
    history.replaceState(null, "", `?mes=${novo.mes}&ano=${novo.ano}`);
    aoMudar(novo);
  };
  selectMes.addEventListener("change", mudou);
  selectAno.addEventListener("change", mudou);

  return el(
    "div",
    { class: "seletor-mes", role: "group", "aria-label": "Período" },
    el("label", { class: "so-leitor", for: "filtro-mes" }, "Mês"),
    selectMes,
    el("label", { class: "so-leitor", for: "filtro-ano" }, "Ano"),
    selectAno,
  );
}
