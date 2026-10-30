# 03 · Metodologia de Desenvolvimento

## 1. Abordagem

Continuamos com **desenvolvimento ágil em sprints semanais** (Scrum leve), como na V1. O trabalho é incremental: cada sprint termina com algo que roda e pode ser demonstrado.

| Elemento | Como fazemos |
|---|---|
| Duração da sprint | 1 semana |
| Planejamento | No início: escolher itens do backlog (GitHub Projects) e definir a meta da sprint |
| Acompanhamento | Quadro Kanban: `Backlog → A fazer → Em andamento → Em revisão → Pronto` |
| Revisão/retrospectiva | No fim: demonstrar o incremento, listar o que funcionou e o que ajustar |
| Priorização | MoSCoW (ver `02-requisitos.md`) |
| Papéis | Todos desenvolvem; um membro rotativo facilita a sprint (planejamento e revisão) |

## 2. Fluxo de trabalho com Git

- Branch principal protegida: `main` (sempre funcional).
- Branches curtas por tarefa: `feature/<resumo>`, `fix/<resumo>`, `docs/<resumo>`.
- **Pull Request obrigatório** para entrar na `main`, com pelo menos **1 revisão** de outro membro.
- **Commits convencionais** (em português, imperativo):
  - `feat: adiciona filtro de transações por mês`
  - `fix: corrige cálculo do saldo`
  - `docs: atualiza diagrama de classes`
  - `refactor:`, `test:`, `chore:`
- Cada tarefa do quadro vira uma *issue*; o PR referencia a issue (`Closes #12`).

## 3. Definition of Ready (pronto para começar)

Uma tarefa só entra na sprint se tiver: objetivo claro, requisitos/regras relacionados (IDs `RF`/`RN`), critério de aceite e estimativa (P/M/G).

## 4. Definition of Done (pronto para entregar)

- [ ] Código compila e a aplicação sobe localmente.
- [ ] Testes novos/ajustados passam (`./mvnw test`).
- [ ] Regras de negócio da tarefa cobertas por teste.
- [ ] Sem segredos, senhas ou dados reais no código.
- [ ] Revisado por outro membro (PR aprovado).
- [ ] **Documentação atualizada** (requisitos, API e diagramas Mermaid, se afetados).
- [ ] Interface verificada em celular (≈360 px) e desktop, quando houver tela.

## 5. Qualidade e testes

| Nível | Ferramenta | O que cobre |
|---|---|---|
| Unitário | JUnit 5 + Mockito | Regras de negócio nos *services* (saldo, alerta, juros, metas) |
| Integração | Spring Boot Test + **Testcontainers (PostgreSQL)** | Repositórios, migrations e endpoints protegidos |
| Aceitação (BDD) | Cucumber (português) | Fluxos: cadastro/login, lançar despesa com alerta, meta, simulador |
| Manual | Roteiro por tela | Responsividade, acessibilidade básica, bugs visuais |

Regra: os passos do Cucumber **chamam o código real** (services/API). Nada de "login simulado" com senha fixa, como na V1.

## 6. Arquitetura e padrões

- **Backend:** MVC em camadas — `controller` (REST/DTO) → `service` (regras) → `repository` (Spring Data JPA) → `model` (entidades).
- **Segurança:** Spring Security, filtro JWT, `PasswordEncoder` BCrypt.
- **Erros:** `@RestControllerAdvice` com resposta padronizada (`status`, `mensagem`, `campos`).
- **Banco:** PostgreSQL com migrations **Flyway** (`db/migration/V1__...sql`).
- **Frontend:** HTML5 + CSS3 (variáveis CSS, mobile-first) + JavaScript modular (ES modules), sem framework.
- **Configuração:** `application.properties` lê variáveis de ambiente (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`).

## 7. Documentação como código

Toda a documentação vive em `docs/` em Markdown, e os diagramas em **Mermaid** (texto versionável). Assim o histórico do Git mostra quando o diagrama mudou e o Claude Code (ou qualquer colega) consegue ler o contexto sem abrir imagens.

| Arquivo | Conteúdo |
|---|---|
| `01-escopo.md` | Visão, escopo, riscos |
| `02-requisitos.md` | RF, RNF, regras de negócio, categorias |
| `03-metodologia.md` | Este documento |
| `04-modelo-de-dados.md` | DER, dicionário e SQL alvo |
| `05-diagramas.md` | Casos de uso, classes, sequência, BPMN, arquitetura |
| `06-api.md` | Contrato dos endpoints |
| `07-roadmap.md` | Sprints, backlog e decisões (ADRs) |
| `08-diagnostico-v1.md` | Problemas encontrados na V1 |
