# IFO — Organizador Financeiro Inteligente

Aplicação web para **controle financeiro pessoal**: registre receitas e despesas, siga a regra **50/30/20**, receba alerta quando um grupo estourar o limite, acompanhe metas e simule investimentos.

Projeto acadêmico da **FATEC Cotia** — Desenvolvimento de Software Multiplataforma (Engenharia de Software II).

> **Status:** em revisão (IFO V1 revisada). A documentação em [`docs/`](docs/) descreve o alvo; veja o roadmap para o andamento.

## Funcionalidades

- Cadastro e login seguros (BCrypt + JWT)
- Transações de receita e despesa com **grupo → categoria → descrição**
  - Grupos: Necessidades · Lazer · Investimentos
  - Exemplo: *Lazer → Delivery → "pizza"*; *Necessidades → Contas fixas → "internet"*
- Filtro por mês e resumo (receitas, despesas, saldo) com gráfico de pizza simples
- **Orçamento 50/30/20** com percentuais editáveis e **alerta de estouro** (atenção a partir de 80%)
- Metas financeiras com aportes e barra de progresso
- Calculadora de investimentos (juros simples e compostos)
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
└── frontend/              # páginas, CSS e JavaScript
docs/                      # escopo, requisitos, diagramas, API, roadmap
docker-compose.yml         # PostgreSQL 15
.env.example               # modelo das variáveis de ambiente
```

## Como executar

Pré-requisitos: Java 17, Docker e um servidor estático simples para o frontend (ex.: extensão Live Server do VS Code).

1. **Variáveis de ambiente**: copie `.env.example` para `.env` na raiz e troque `DB_PASSWORD` e `JWT_SECRET` (mínimo de 32 caracteres). O `.env` é lido pelo Docker Compose e pelo backend, e **nunca** deve ser versionado.

   ```bash
   cp .env.example .env
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

4. **Frontend**

   ```bash
   cd ifo/frontend
   python3 -m http.server 5500
   ```

   Abra `http://localhost:5500`.

5. **Testes** (por enquanto usam o PostgreSQL do Docker Compose; Testcontainers entra na Sprint R5)

   ```bash
   cd ifo/backend/ifo-backend
   ./mvnw test
   ```

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
