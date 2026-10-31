# IFO — Organizador Financeiro Inteligente

Aplicação web para **controle financeiro pessoal**: registre receitas e despesas, siga a regra **50/30/20**, receba alerta quando um grupo estourar o limite, acompanhe metas e simule investimentos.

Projeto acadêmico da **FATEC Cotia** — Desenvolvimento de Software Multiplataforma (Engenharia de Software II).

> **Status:** IFO V1 revisada. Backend (R1–R3), frontend (R4), testes de integração, revisão de segurança, guia de execução e PDF do seminário atualizado (R5, partes 1 a 4) concluídos. **Todas as sprints estão concluídas.** A documentação em [`docs/`](docs/) descreve o sistema; o [roadmap](docs/07-roadmap.md) mostra o andamento e as decisões (ADRs).

## Funcionalidades

- Cadastro e login seguros (BCrypt + JWT)
- Transações de receita e despesa com **grupo → categoria → descrição**
  - Grupos: Necessidades · Lazer · Investimentos
  - Exemplo: *Lazer → Delivery → "pizza"*; *Necessidades → Contas fixas → "internet"*
- Filtro por mês e resumo (receitas, despesas, saldo) com gráfico de pizza simples
- **Orçamento 50/30/20** com percentuais editáveis e **alerta de estouro** (atenção a partir de 80%)
- Metas financeiras com aportes e barra de progresso
- Simulador de investimentos (juros simples e compostos)
- **Despesas recorrentes** (ex.: internet, luz) geradas automaticamente todo mês
- **Exportar CSV** das transações do mês
- **Dark mode** (tema claro/escuro)

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Backend | Java 17, Spring Boot 3.2, Spring Data JPA, Spring Security (JWT), Flyway |
| Banco de dados | PostgreSQL 15 (Docker) |
| Frontend | HTML5, CSS3 e JavaScript (sem frameworks) |
| Testes | JUnit 5, Mockito, Testcontainers, Cucumber |
| Arquitetura | MVC em camadas + API REST (JSON) |

## Estrutura do repositório

```text
ifo/
├── backend/ifo-backend/   # API Spring Boot (migrations em src/main/resources/db/migration)
│   └── dev/dev-seed.sql   # dados de demonstração (só para desenvolvimento)
└── frontend/              # uma página HTML por tela, css/ (tokens, base, componentes) e js/ (módulos ES)
docs/                      # escopo, requisitos, diagramas, API, roadmap
docker-compose.yml         # PostgreSQL 15
.env.example               # modelo das variáveis de ambiente
```

## Como executar

Pré-requisitos: **Java 17**, **Docker** (ligado) e um servidor estático simples para o frontend (ex.: extensão Live Server do VS Code). O frontend usa módulos ES, então **precisa** ser aberto por um servidor (`http://localhost:5500`), não por duplo clique no arquivo.

1. **Variáveis de ambiente**: copie `.env.example` para `.env` na raiz e troque `DB_PASSWORD` e `JWT_SECRET`. O `.env` é lido pelo Docker Compose e pelo backend, e **nunca** deve ser versionado.

   ```bash
   cp .env.example .env
   ```

   No PowerShell: `Copy-Item .env.example .env`.

   O `JWT_SECRET` precisa ter **no mínimo 32 caracteres aleatórios**. A API **se recusa a subir** com o valor de exemplo (o que começa com `troque`). Para gerar uma chave:

   ```bash
   openssl rand -base64 48
   ```

   No PowerShell, sem `openssl`:

   ```powershell
   $b = New-Object byte[] 48; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
   ```

2. **Banco de dados**

   ```bash
   docker compose up -d db
   ```

3. **Backend** (porta 8081; as migrations Flyway criam as tabelas e as categorias). Também dá para rodar pela IDE, executando `IfoBackendApplication`.

   ```bash
   cd ifo/backend/ifo-backend
   ./mvnw spring-boot:run
   ```

   No Windows, use o PowerShell e `.\mvnw.cmd spring-boot:run` (no Git Bash o `./mvnw` costuma falhar).

4. **Frontend**

   Com o Live Server do VS Code, abra a pasta `ifo/frontend` e clique em *Go Live* (porta 5500). Sem VS Code, qualquer servidor estático serve:

   ```bash
   cd ifo/frontend
   python3 -m http.server 5500
   ```

   Abra `http://localhost:5500` (a API só aceita as origens `http://localhost:5500` e `http://127.0.0.1:5500`; para mudar, ajuste `CORS_ORIGINS` no `.env`). Crie uma conta na tela de cadastro e informe sua renda: sem renda não há limites de orçamento nem alertas. Se preferir ver o sistema já com dados, use a seção abaixo.

5. **Testes**

   ```bash
   cd ifo/backend/ifo-backend
   ./mvnw test
   ```

   No Windows, `.\mvnw.cmd test` no PowerShell. Os testes de integração e o Cucumber sobem **o próprio PostgreSQL** com Testcontainers: só precisam do Docker ligado (na primeira vez ele baixa as imagens). Não dependem do `.env` nem do container `ifo-db`.

## Backend em Docker (usado na publicação)

O `ifo/backend/ifo-backend/Dockerfile` gera a mesma imagem que roda na nuvem (ADR-018). Para testar localmente, com o `ifo-db` ligado:

```bash
cd ifo/backend/ifo-backend
docker build -t ifo-backend .
docker run --rm --env-file ../../../.env -e DB_URL=jdbc:postgresql://host.docker.internal:5432/ifo_db -e PORT=10000 -p 10000:10000 ifo-backend
```

A imagem usa o perfil `prod`, o fuso `America/Sao_Paulo` e lê a porta da variável `PORT` (sem ela, 8081). Nenhum segredo vai dentro da imagem e a pasta `dev/` (com o `dev-seed.sql`) fica de fora. A seção de publicação completa entra na R6, Parte 6.

## Dados de demonstração (opcional)

O arquivo [`dev-seed.sql`](ifo/backend/ifo-backend/dev/dev-seed.sql) cria dois usuários com transações dos últimos três meses, metas e despesas recorrentes, para ver o sistema funcionando sem digitar nada. Rode **depois** de o backend ter subido ao menos uma vez (o Flyway cria as tabelas). A partir da raiz do repositório:

```bash
docker cp ifo/backend/ifo-backend/dev/dev-seed.sql ifo-db:/tmp/dev-seed.sql
docker exec ifo-db sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 -f /tmp/dev-seed.sql'
```

Os comandos funcionam no PowerShell, no Git Bash e no Linux/macOS. O seed é seguro para repetir: ele apaga e recria **só** os dois usuários abaixo (e os dados deles).

| Usuário | Senha | O que mostra |
|---|---|---|
| `demo@ifo.teste` | `demo12345` | Renda de R$ 5.000. No mês atual: Necessidades em **atenção** (86,8%), Lazer em **estouro** (110%), Investimentos **ok** (60%). Três metas (uma concluída), uma recorrência ativa (Internet) e uma pausada (Academia) |
| `outro@ifo.teste` | `demo12345` | Renda de R$ 3.000, com poucas transações. Serve para mostrar que um usuário **não enxerga** os dados do outro |

> A internet do mês atual aparece quando o job diário roda (00:05) ou na hora em que você edita a recorrência na tela. Essas senhas são só de demonstração: **nunca** use o seed em um banco de verdade.

## Rotas da API

Todas começam com `/api` e, exceto `/auth/cadastro` e `/auth/login`, exigem `Authorization: Bearer <token>` (inclusive o simulador, que não grava nada, mas só atende quem está logado). O contrato completo (corpos, respostas e erros) está em [06 · API](docs/06-api.md).

| Recurso | Rotas |
|---|---|
| Autenticação | `POST /auth/cadastro` · `POST /auth/login` |
| Usuário | `GET /usuarios/me` · `PUT /usuarios/me` |
| Categorias | `GET /categorias` |
| Transações | `GET /transacoes?mes&ano` · `POST /transacoes` · `PUT /transacoes/{id}` · `DELETE /transacoes/{id}` · `GET /transacoes/exportar?mes&ano` (CSV) |
| Resumo | `GET /resumo?mes&ano` |
| Orçamento | `GET /orcamento` · `PUT /orcamento` · `GET /orcamento/status?mes&ano` |
| Metas | `GET /metas` · `POST /metas` · `PUT /metas/{id}` · `DELETE /metas/{id}` · `POST /metas/{id}/aportes` |
| Simulador | `POST /investimentos/simular` |
| Recorrências | `GET /recorrencias` · `POST /recorrencias` · `PUT /recorrencias/{id}` · `DELETE /recorrencias/{id}` · `PATCH /recorrencias/{id}/ativa` |

## Segurança

- Senhas guardadas com **BCrypt**; sessão por **JWT** sem estado. Segredos ficam no `.env` (fora do Git); a API recusa um `JWT_SECRET` curto ou igual ao de exemplo.
- Todo dado é filtrado pelo usuário do token. O que é de outro usuário responde **404**, igual a um dado que não existe (RN11). Isso é coberto por testes automatizados (`isolamento.feature` e `acesso.feature`).
- O CSV neutraliza injeção de fórmula (`= + - @`, TAB e CR no início do texto).
- Riscos conhecidos e aceitos (projeto acadêmico), registrados no [ADR-016](docs/07-roadmap.md): sem limite de tentativas de login, token no `localStorage`, token sem revogação no servidor.

## Qualidade e testes

- **Backend:** testes unitários (JUnit 5 + Mockito) para as regras de negócio e **Cucumber em português chamando a API por HTTP** (login, transações com alerta, metas, simulador, recorrências, isolamento entre usuários), contra um PostgreSQL real do Testcontainers.
- **Frontend:** não há testes automatizados no repositório (decisão do [ADR-017](docs/07-roadmap.md)). Na R4 a interface foi verificada fora do repositório com jsdom, Chrome sem interface e `axe-core`, sem violações de acessibilidade WCAG 2.x A/AA, e o checklist por tela está no [roadmap](docs/07-roadmap.md).

## Problemas comuns

| Sintoma | Causa e solução |
|---|---|
| `JWT_SECRET ainda é o valor de exemplo` ao subir a API | Você não trocou a chave do `.env`. Gere uma nova (passo 1) |
| `JWT_SECRET precisa ter pelo menos 32 caracteres` | A chave do `.env` é curta demais |
| `Could not find a valid Docker environment` ao rodar `test` | O Docker não está ligado. Abra o Docker Desktop e tente de novo |
| Erro de conexão com o banco ao subir a API | O container não está de pé: `docker compose up -d db` e confira `DB_URL`, `DB_USER` e `DB_PASSWORD` |
| O seed falha com `relation "usuario" does not exist` | O backend ainda não rodou. Suba a API uma vez (passo 3) e repita |
| O frontend carrega mas toda chamada falha (CORS) | Abra por `http://localhost:5500` (ou `127.0.0.1:5500`), não por duplo clique, ou ajuste `CORS_ORIGINS` |
| `./mvnw` dá erro de `fork` no Git Bash | Use o PowerShell com `.\mvnw.cmd` |

## Documentação

| Documento | Conteúdo |
|---|---|
| [01 · Escopo](docs/01-escopo.md) | Visão, objetivos, dentro/fora do escopo, riscos |
| [02 · Requisitos](docs/02-requisitos.md) | Requisitos funcionais e não funcionais, regras de negócio, categorias |
| [03 · Metodologia](docs/03-metodologia.md) | Sprints, Git, Definition of Done, testes |
| [04 · Modelo de dados](docs/04-modelo-de-dados.md) | DER, dicionário e migrations |
| [05 · Diagramas](docs/05-diagramas.md) | Casos de uso, classes, sequência, BPMN, arquitetura |
| [06 · API](docs/06-api.md) | Contrato dos endpoints |
| [07 · Roadmap](docs/07-roadmap.md) | Sprints, backlog e decisões (ADRs) |
| [08 · Diagnóstico da V1](docs/08-diagnostico-v1.md) | Problemas encontrados na primeira versão |

## Equipe

- João Marcos Vilaça Marques
- Amanda Freitas de Lima
- Lucas de Souza Saldanha dos Santos
- Vinicius de Amorim Farias

## Próximos passos (IFO V2)

Dashboards interativos, integração bancária, aplicativo mobile, autenticação em dois fatores e sugestões com IA.
