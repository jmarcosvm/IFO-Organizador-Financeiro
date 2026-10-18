# 07 · Roadmap, Backlog e Decisões

## 1. Visão geral das sprints

A V1 teve as Sprints 0–4. A refatoração continua a numeração com o prefixo **R** (R0–R5), uma semana cada.

| Sprint | Meta | Entregas principais |
|---|---|---|
| **R0 — Documentação** | Contexto fechado e versionado | `docs/`, diagramas Mermaid, README, PDF do seminário atualizado |
| **R1 — Fundação** | Backend alinhado ao banco e seguro | Flyway (V1/V2), entidades novas, DTOs, BCrypt + JWT, `@RestControllerAdvice`, variáveis de ambiente, `docker-compose`, remoção de Conta/Investimento |
| **R2 — Núcleo financeiro** | Transações de ponta a ponta | Categorias fixas, CRUD de transações (PUT), filtro por mês, resumo mensal |
| **R3 — Orçamento, metas, calculadora e recorrências** | Regras de negócio completas | Orçamento editável, status e alerta, metas com aportes, calculadora (juros simples/compostos), despesas recorrentes com job diário, exportação CSV |
| **R4 — Frontend** | Interface nova e responsiva | Design system (tokens CSS), mobile-first, telas refeitas, pizza simples, tema claro/escuro, telas de recorrências, botão exportar CSV, correção de bugs visuais, escape de HTML |
| **R5 — Qualidade e entrega** | Pronto para apresentar | Testes (unitário, integração, Cucumber), revisão de segurança, README final, vídeo/slides |

> As sprints R3 e R4 podem andar em paralelo por pessoas diferentes depois que a API de R2 estiver estável.

## 2. Backlog detalhado

### R1 — Fundação
- [ ] Criar `docker-compose.yml` com PostgreSQL 15 (credenciais por `.env`).
- [ ] Adicionar Flyway; criar `V1__criar_esquema.sql` e `V2__categorias_fixas.sql` (ver `04-modelo-de-dados.md`; inclui a tabela `transacao_recorrente`).
- [ ] Trocar `ddl-auto=none` por `validate`; mover segredos para variáveis de ambiente.
- [ ] Reescrever entidades: `Usuario` (senhaHash), `Categoria` (nome, tipo, grupo), `Transacao` (ManyToOne), `Orcamento`, `MetaFinanceira`.
- [ ] Remover: `ContaCarteira`, `Investimento` (entidade/repo/service/controller), `TipoConta`, `TipoOrcamento`, enum antigo `TipoCategoria`.
- [ ] Spring Security + filtro JWT + BCrypt; `AuthController` (`/auth/cadastro`, `/auth/login`).
- [ ] DTOs de entrada/saída; nenhuma entidade na resposta.
- [ ] `@RestControllerAdvice` com formato de erro padrão (`06-api.md` §8).
- [ ] CORS restrito à origem do frontend.

### R2 — Núcleo financeiro
- [ ] `GET /categorias`.
- [ ] `TransacaoService`: criar/atualizar/excluir/listar por mês, sempre filtrando pelo usuário do token.
- [ ] Derivar tipo e grupo da categoria (RN02); validar valor > 0.
- [ ] `GET /resumo` (receitas, despesas, saldo, despesas por grupo).
- [ ] Testes unitários do saldo e do resumo.

### R3 — Orçamento, metas, calculadora e recorrências
- [ ] Criar orçamento padrão 50/30/20 no cadastro.
- [ ] `GET/PUT /orcamento` com validação de soma = 100.
- [ ] `GET /orcamento/status` e alerta embutido na resposta de `POST/PUT /transacoes`.
- [ ] CRUD de metas + `POST /metas/{id}/aportes` + `progresso` e `concluida`.
- [ ] `POST /investimentos/simular` com `BigDecimal` (caso de referência 3804,75).
- [ ] Testes: níveis 79,9% / 80% / 100% / 100,01%; renda zero; soma ≠ 100.
- [ ] CRUD de recorrências (`/recorrencias`) com validação de categoria DESPESA (RN14).
- [ ] Job diário `@Scheduled` + geração ao criar a recorrência; avanço por `ultima_competencia`, sem duplicar (RN15–RN17).
- [ ] Testes: dia 31 em mês curto, mês perdido (catch-up), rodar o job duas vezes, recorrência pausada/encerrada.
- [ ] `GET /transacoes/exportar` (CSV `;`, BOM UTF-8, vírgula decimal, neutralização de `=+-@`) (RN18).

### R4 — Frontend
- [ ] Definir design tokens (cores, tipografia, espaçamento, raios) em variáveis CSS.
- [ ] Layout **mobile-first** (360 px) com breakpoints para tablet e desktop; menu adaptado para celular.
- [ ] Reescrever `app.js` em módulos ES (`api.js`, `auth.js`, `ui.js` e um arquivo por tela).
- [ ] Cliente de API com token e tratamento central de erros (401 volta ao login).
- [ ] Telas: login, cadastro, dashboard, transações (filtro de mês), orçamento, metas, calculadora, perfil.
- [ ] Formulário de transação: **categoria → (grupo exibido automaticamente) → descrição**.
- [ ] Alerta de orçamento (faixa amarela/vermelha) no lançamento e no dashboard.
- [ ] Gráfico de pizza simples (SVG ou `conic-gradient`, sem biblioteca).
- [ ] Tela de **despesas recorrentes** (listar, criar, editar, pausar, excluir).
- [ ] Botão **Exportar CSV** na tela de transações, respeitando o filtro de mês.
- [ ] **Dark mode:** tokens de cor em `:root` e `[data-theme="dark"]`, padrão por `prefers-color-scheme`, botão de alternar, escolha em `localStorage`; conferir contraste AA nos dois temas e as cores do gráfico de pizza.
- [ ] Trocar `innerHTML` com dados do usuário por `textContent`/escape.
- [ ] Levantar e corrigir bugs visuais (checklist por tela abaixo).
- [ ] Acessibilidade básica: `label`, foco visível, contraste AA, teclado.

### R5 — Qualidade e entrega
- [ ] Testcontainers (PostgreSQL) para integração.
- [ ] Reescrever Cucumber chamando o código real (login, transação com alerta, meta, calculadora).
- [ ] Revisão de segurança: ninguém acessa dado alheio; segredos fora do repo.
- [ ] README final, `dev-seed.sql`, passo a passo de execução.
- [ ] Atualizar o PDF do seminário com os diagramas novos.

## 3. Checklist de bugs visuais (preencher na R4)

Marcar por tela, em celular (360 px) e desktop (1280 px):

| Tela | Sobreposição | Texto cortado | Menu/overflow | Estados vazios | Erros visíveis | Foco/contraste |
|---|---|---|---|---|---|---|
| Login / Cadastro | ☐ | ☐ | ☐ | — | ☐ | ☐ |
| Dashboard | ☐ | ☐ | ☐ | ☐ | ☐ | ☐ |
| Transações | ☐ | ☐ | ☐ | ☐ | ☐ | ☐ |
| Orçamento | ☐ | ☐ | ☐ | ☐ | ☐ | ☐ |
| Metas | ☐ | ☐ | ☐ | ☐ | ☐ | ☐ |
| Calculadora | ☐ | ☐ | ☐ | — | ☐ | ☐ |

## 4. Backlog IFO V2 (futuro, fora deste ciclo)

- Dashboards interativos (gráficos dinâmicos, cartões, comparativos mês a mês).
- Integração com APIs bancárias (importação de transações).
- Aplicativo mobile nativo/híbrido.
- Autenticação de dois fatores.
- Sugestões de economia com IA.


## 5. Decisões de arquitetura (ADRs)

| ID | Decisão | Motivo |
|---|---|---|
| ADR-001 | Categorias **fixas**, em tabela carregada por migration (não enum) | Permite FK, grupo por categoria e evolução sem recompilar; usuário só edita a descrição |
| ADR-002 | Tipo e grupo da transação **derivados da categoria** | Elimina inconsistência (ex.: "Delivery" marcado como Necessidade) |
| ADR-003 | Calculadora **sem persistência**, calculada no backend | Escopo pedido: só calculadora; backend com `BigDecimal` é testável e fonte única da regra |
| ADR-004 | **BCrypt + JWT** stateless | Resolve senha em texto puro e remove `?usuario=ID` |
| ADR-005 | **Flyway** + `ddl-auto=validate` | Esquema reproduzível; fim da divergência entre SQL manual e entidades |
| ADR-006 | Frontend em **HTML/CSS/JS puro** com módulos ES; pizza sem biblioteca | Mantém decisão da V1 (leveza, controle) |
| ADR-007 | **Remover Conta/Carteira** | Não será usada; simplifica modelo e diagramas |
| ADR-008 | Alerta **informativo**, limiar de atenção em 80%, nunca bloqueia | Pedido: "notificação simples de estouro" |
| ADR-009 | Dashboards interativos **adiados** para o IFO V2 | Controle de escopo |
| ADR-010 | Diagramas em **Mermaid** versionados em `docs/` | Documentação sincronizada com o código e legível pelo Claude Code |
| ADR-011 | **Despesas recorrentes** como modelo + job diário idempotente (`ultima_competencia`), só para DESPESA | Aprovado em 18/10/2026; evita duplicar, recupera meses perdidos e mantém a transação gerada como transação comum |
| ADR-012 | **Exportar CSV** com `;`, vírgula decimal e UTF-8 com BOM | Abre direto no Excel em português; neutraliza injeção de fórmulas |
| ADR-013 | **Dark mode** por variáveis CSS, `prefers-color-scheme` e `localStorage` | Sem biblioteca; reaproveita os design tokens da R4 |

Novas decisões entram nesta tabela com número sequencial.
