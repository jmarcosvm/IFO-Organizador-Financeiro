# 06 · Contrato da API (alvo da V1 revisada)

Base: `http://localhost:8081/api` · Formato: JSON · Autenticação: `Authorization: Bearer <token>` (exceto `/auth/*`).

O `id` do usuário **nunca** vai na URL ou no corpo: o backend lê do token (RNF02). Entidades JPA **não** são expostas; tudo passa por DTOs.

## 1. Autenticação (público)

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| POST | `/auth/cadastro` | `{ nome, email, senha, rendaMensal }` | `201 { token, usuario }` |
| POST | `/auth/login` | `{ email, senha }` | `200 { token, usuario }` · `401` se inválido |

`usuario` = `{ idUsuario, nome, email, rendaMensal }` (sem senha/hash).
O cadastro cria o orçamento padrão 50/30/20 (RF11).

## 2. Usuário

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| GET | `/usuarios/me` | — | `200 usuario` |
| PUT | `/usuarios/me` | `{ nome, rendaMensal }` | `200 usuario` |

## 3. Categorias (somente leitura)

| Método | Rota | Resposta |
|---|---|---|
| GET | `/categorias` | `200 [ { idCategoria, nome, tipo, grupo } ]` |

## 4. Transações

| Método | Rota | Corpo / Query | Resposta |
|---|---|---|---|
| GET | `/transacoes?mes=10&ano=2026` | mês/ano opcionais (padrão: mês atual) | `200 [ transacao ]` ordenadas por data desc |
| POST | `/transacoes` | `{ idCategoria, valor, data, descricao }` | `201 { transacao, alerta }` (`alerta` é `null` se não houver) |
| PUT | `/transacoes/{id}` | mesmo corpo do POST | `200 { transacao, alerta }` · `404` se não for do usuário |
| DELETE | `/transacoes/{id}` | — | `204` · `404` se não for do usuário |
| GET | `/resumo?mes=10&ano=2026` | — | `200 resumo` |

```json
// transacao
{ "idTransacao": 12, "valor": 55.90, "data": "2026-10-17", "descricao": "pizza",
  "categoria": { "idCategoria": 14, "nome": "Delivery", "tipo": "DESPESA", "grupo": "LAZER" } }

// alerta (quando aplicável)
{ "grupo": "LAZER", "nivel": "ESTOURO", "percentualUsado": 104.2,
  "limite": 450.00, "gasto": 469.00,
  "mensagem": "Você ultrapassou o limite de Lazer deste mês." }

// resumo
{ "mes": 10, "ano": 2026, "totalReceitas": 1500.00, "totalDespesas": 1200.00, "saldo": 300.00,
  "despesasPorGrupo": [
    { "grupo": "NECESSIDADES", "total": 700.00, "percentual": 58.3 },
    { "grupo": "LAZER", "total": 300.00, "percentual": 25.0 },
    { "grupo": "INVESTIMENTOS", "total": 200.00, "percentual": 16.7 } ] }
```

Validações: `valor > 0`; `idCategoria` existente; `data` obrigatória; `descricao` até 255 caracteres.

## 5. Orçamento

| Método | Rota | Corpo / Query | Resposta |
|---|---|---|---|
| GET | `/orcamento` | — | `200 [ { grupo, percentual } ]` |
| PUT | `/orcamento` | `{ "NECESSIDADES": 50, "LAZER": 30, "INVESTIMENTOS": 20 }` | `200` lista atualizada · `400` se a soma ≠ 100 |
| GET | `/orcamento/status?mes=10&ano=2026` | — | `200 [ statusGrupo ]` |

```json
// statusGrupo
{ "grupo": "LAZER", "percentual": 30.0, "limite": 450.00, "gasto": 380.00,
  "percentualUsado": 84.4, "nivel": "ATENCAO" }
```

`nivel`: `OK` (< 80%), `ATENCAO` (80–100%), `ESTOURO` (> 100%). Se a renda for 0, `limite` vem `null` e `nivel` vem `SEM_LIMITE`.

## 6. Metas

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| GET | `/metas` | — | `200 [ meta ]` |
| POST | `/metas` | `{ tituloMeta, valorObjetivo, prazo }` | `201 meta` |
| PUT | `/metas/{id}` | `{ tituloMeta, valorObjetivo, prazo }` | `200 meta` |
| DELETE | `/metas/{id}` | — | `204` |
| POST | `/metas/{id}/aportes` | `{ valor }` (> 0) | `200 meta` |

```json
{ "idMeta": 3, "tituloMeta": "Notebook", "valorObjetivo": 3000.00, "valorAtual": 450.00,
  "prazo": "2026-12-31", "progresso": 15.0, "concluida": false }
```

## 7. Simulador de investimentos (sem persistência)

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| POST | `/investimentos/simular` | ver abaixo | `200 resultado` |

```json
// entrada
{ "aplicacaoInicial": 0, "valorMensal": 300.00, "taxaMensal": 1.0, "tempoMeses": 12, "tipoJuros": "COMPOSTO" }

// resultado
{ "montanteFinal": 3804.75, "totalInvestido": 3600.00, "rendimento": 204.75,
  "evolucao": [ { "mes": 1, "investido": 300.00, "montante": 300.00 }, "..." ] }
```

Fórmulas em `02-requisitos.md` (RN13). Caso de teste de referência: entrada acima → **3804,75**.

## 8. Formato de erro (todas as rotas)

```json
{ "timestamp": "2026-10-18T12:00:00", "status": 400, "erro": "Requisição inválida",
  "mensagem": "A soma dos percentuais deve ser 100.",
  "campos": { "percentual": "obrigatório" } }
```

| Status | Quando |
|---|---|
| 400 | Validação falhou, regra de negócio violada |
| 401 | Token ausente, inválido ou expirado; login incorreto |
| 404 | Recurso não existe **ou não pertence ao usuário** |
| 409 | E-mail já cadastrado |
| 500 | Erro inesperado (mensagem genérica, sem stack trace) |

## 9. Despesas recorrentes

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| GET | `/recorrencias` | — | `200 [ recorrencia ]` |
| POST | `/recorrencias` | `{ idCategoria, valor, descricao, diaMes, dataInicio, dataFim }` | `201 recorrencia` (já gera as ocorrências vencidas) |
| PUT | `/recorrencias/{id}` | mesmo corpo do POST | `200 recorrencia` (afeta só meses futuros) |
| PATCH | `/recorrencias/{id}/ativa` | `{ "ativa": false }` | `200 recorrencia` |
| DELETE | `/recorrencias/{id}` | — | `204` (transações já geradas permanecem) |

```json
{ "idRecorrencia": 5, "valor": 99.90, "descricao": "internet", "diaMes": 10,
  "dataInicio": "2026-10-01", "dataFim": null, "ativa": true, "ultimaCompetencia": "2026-10-01",
  "categoria": { "idCategoria": 8, "nome": "Contas fixas", "tipo": "DESPESA", "grupo": "NECESSIDADES" } }
```

Validações: categoria existente e de tipo `DESPESA`; `valor > 0`; `diaMes` de 1 a 31; `dataFim` ≥ `dataInicio`. O job diário (`@Scheduled`) segue as regras RN14–RN17.

## 10. Exportação CSV

| Método | Rota | Resposta |
|---|---|---|
| GET | `/transacoes/exportar?mes=10&ano=2026` | `200 text/csv; charset=UTF-8` com `Content-Disposition: attachment; filename="ifo-transacoes-2026-10.csv"` |

```csv
Data;Tipo;Grupo;Categoria;Descrição;Valor
17/10/2026;DESPESA;LAZER;Delivery;pizza;55,90
10/10/2026;DESPESA;NECESSIDADES;Contas fixas;internet;99,90
05/10/2026;RECEITA;;Salário;pagamento;1500,00
```

O arquivo começa com BOM UTF-8. O frontend baixa com `fetch` enviando o token e cria o arquivo a partir do `Blob`. Regras em RN18.

> O **tema claro/escuro** (RF22) é só frontend: não existe endpoint.
