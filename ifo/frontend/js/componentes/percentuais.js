// Prévia da soma dos três percentuais do orçamento (RN05: tem que dar exatamente 100).
// É só ajuda visual: quem decide é o backend. Soma em centésimos de ponto percentual
// (inteiros) para 33,33 + 33,33 + 33,34 dar 100 e não 100,00000000000001.

import { percentual } from "../formato.js";

export const TOTAL_CENTESIMOS = 10000;

export function somaEmCentesimos(valores) {
  return valores.reduce((acc, v) => {
    const n = Number(v);
    return acc + (Number.isFinite(n) ? Math.round(n * 100) : 0);
  }, 0);
}

// Devolve { fecha, texto } para exibir ao lado dos campos
export function descreverSoma(centesimos) {
  const soma = centesimos / 100;
  if (centesimos === TOTAL_CENTESIMOS) return { fecha: true, texto: `Soma: ${percentual(soma)}. Pronto para salvar.` };
  const diferenca = Math.abs(TOTAL_CENTESIMOS - centesimos) / 100;
  return {
    fecha: false,
    texto:
      centesimos < TOTAL_CENTESIMOS
        ? `Soma: ${percentual(soma)}. Faltam ${percentual(diferenca)} para chegar a 100%.`
        : `Soma: ${percentual(soma)}. Passou ${percentual(diferenca)} de 100%.`,
  };
}
