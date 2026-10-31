# IFO — Organizador Financeiro Inteligente

Aplicação web para **controle financeiro pessoal**: registre receitas e despesas, siga a regra **50/30/20**, receba alerta quando um grupo estourar o limite, acompanhe metas e simule investimentos.

Projeto acadêmico da **FATEC Cotia** — Desenvolvimento de Software Multiplataforma (Engenharia de Software II).

> **Status:** IFO V1 revisada. Backend (R1–R3), frontend (R4), testes de integração, revisão de segurança, guia de execução e PDF do seminário atualizado (R5, partes 1 a 4) concluídos. **As sprints R0–R5 estão concluídas e o IFO está no ar (R6): veja [Experimente online](#experimente-online).** A documentação em [`docs/`](docs/) descreve o sistema; o [roadmap](docs/07-roadmap.md) mostra o andamento e as decisões (ADRs).

## Experimente online

**https://ifo-organizador-financeiro-frontend.vercel.app**

Para ver o sistema sem criar conta, entre com o usuário de demonstração:

| E-mail | Senha |
|---|---|
| `demo@ifo.teste` | `demo12345` |

> **Conta pública e compartilhada:** qualquer visitante pode entrar nela e alterar ou apagar os dados. Não coloque informações pessoais. Para um uso seu, crie uma conta pela tela de cadastro.
>
> **O servidor dorme quando ninguém usa** (hospedagem gratuita). Na primeira visita depois de um tempo parado, o login pode levar até 1 minuto; o site mostra um aviso "servidor acordando". Depois disso fica rápido.

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
- Interface fluida: transição suave entre telas, esqueletos de carregamento e pequenas animações (desligadas para quem usa "reduzir movimento" no sistema)

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Backend | Java 17, Spring Boot 3.2, Spring Data JPA, Spring Security (JWT), Flyway |
| Banco de dados | PostgreSQL 15 (Docker, no computador) · PostgreSQL no Neon (na nuvem) |
| Publicação | Vercel (frontend) · Render com Docker (backend) · Neon (banco), tudo no plano gratuito (ADR-018) |
| Frontend | HTML5, CSS3 e JavaScript (sem frameworks) |
| Testes | JUnit 5, Mockito, Testcontainers, Cucumber |
| Arquitetura | MVC em camadas + API REST (JSON) |

## Estrutura do repositório

```text
ifo/
├── backend/ifo-backend/   # API Spring Boot (migrations em src/main/resources/db/migration)
│   ├── Dockerfile         # imagem usada na publicação (Render)
│   └── dev/dev-seed.sql   # dados de demonstração (só para desenvolvimento; nunca vai para a nuvem)
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

   O frontend escolhe a API pelo endereço da página: em `localhost`, `127.0.0.1` ou `[::1]` usa a API local (`http://localhost:8081/api`); em qualquer outro endereço (Vercel) usa a API publicada no Render (ADR-018). A constante fica em `ifo/frontend/js/api.js`.

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

A imagem usa o perfil `prod`, o fuso `America/Sao_Paulo` e lê a porta da variável `PORT` (sem ela, 8081). Nenhum segredo vai dentro da imagem e a pasta `dev/` (com o `dev-seed.sql`) fica de fora. A publicação na nuvem está descrita na seção seguinte.

## Publicação na web (deploy)

O IFO roda de graça na nuvem, em três peças (decisão no [ADR-018](docs/07-roadmap.md)):

| Peça | Serviço | Endereço |
|---|---|---|
| Frontend (HTML, CSS e JS) | Vercel, plano Hobby | https://ifo-organizador-financeiro-frontend.vercel.app |
| Backend (Spring Boot em Docker) | Render, Web Service gratuito | https://ifo-api.onrender.com |
| Banco (PostgreSQL) | Neon, plano gratuito | privado (só o backend acessa) |

```text
Navegador ──► Vercel (arquivos estáticos)
     │
     └─ HTTPS (fetch, só desta origem pelo CORS) ──► Render (API) ──► Neon (PostgreSQL)
```

A cada merge na `master`, o Render e a Vercel publicam a versão nova sozinhos. As migrations Flyway rodam na subida da API.

### Variáveis de ambiente do Render

Ficam só no painel do Render, **nunca** no Git. A imagem já define `SPRING_PROFILES_ACTIVE=prod` e o fuso `America/Sao_Paulo`, e o Render fornece a `PORT`.

| Variável | Valor |
|---|---|
| `DB_URL` | `jdbc:postgresql://<host-direto-do-neon>/<banco>?sslmode=require` (host **sem** `-pooler`, sem usuário nem senha na URL e sem `channel_binding`) |
| `DB_USER` | usuário do banco no Neon |
| `DB_PASSWORD` | senha do banco no Neon |
| `JWT_SECRET` | chave nova e aleatória (veja o passo 1 de "Como executar"); **diferente** da do seu `.env` local |
| `JWT_EXPIRACAO_MINUTOS` | `480` |
| `CORS_ORIGINS` | `https://ifo-organizador-financeiro-frontend.vercel.app` (sem `/` no final e **sem** `localhost`) |

### Como refazer a publicação do zero

1. **Neon:** crie o projeto e copie a conexão **direta** (desligue o "Connection pooling" na janela *Connect*).
2. **Render:** *New → Web Service* a partir do repositório. *Language* `Docker`, *Branch* `master`, *Dockerfile Path* `ifo/backend/ifo-backend/Dockerfile`, *Docker Build Context Directory* `ifo/backend/ifo-backend`, plano **Free** e as variáveis da tabela acima.
3. **Vercel:** *Add New → Project*, importe o repositório, *Root Directory* `ifo/frontend`, *Framework Preset* `Other` e sem comando de build.
4. **CORS:** ajuste `CORS_ORIGINS` no Render para o endereço da Vercel e espere o serviço voltar a ficar *Live*.
5. Se o endereço do backend mudar, troque a URL de produção em [`ifo/frontend/js/api.js`](ifo/frontend/js/api.js). O frontend usa a API local em `localhost`, `127.0.0.1` e `[::1]`, e a de produção em qualquer outro endereço.

### O que esperar da hospedagem gratuita

- **O Render dorme** depois de 15 minutos sem tráfego. A primeira visita leva até 1 minuto e o site avisa. As despesas recorrentes são geradas também ao subir a API; o gerador recupera os meses perdidos e nunca duplica.
- **O Neon** guarda 1 GB e suspende o banco após 5 minutos parado (acorda sozinho). Não há backup automático além de 6 horas de histórico. Se as horas do mês acabarem, o banco suspende até o mês seguinte, sem apagar dados.
- **A Vercel Hobby** é para uso não comercial, o que serve para portfólio e projeto acadêmico.
- O `dev-seed.sql` **nunca** é aplicado na nuvem: a conta de demonstração do ar foi criada pela tela de cadastro.

### Roteiro de verificação no ar

Use depois de qualquer mudança de publicação (o servidor pode demorar 1 minuto no primeiro acesso):

1. Entrar com a conta de demonstração. O dashboard carrega.
2. Lançar uma **despesa que estoure o limite** de um grupo. Aparece o **alerta de orçamento** e o lançamento é salvo mesmo assim (RN08).
3. **Orçamento:** os percentuais padrão são 50/30/20; a soma precisa dar 100.
4. **Simulador:** aporte mensal 300, taxa 1% ao mês, 12 meses, juros compostos. O resultado é **R$ 3.804,75**.
5. **Exportar CSV** na tela de transações: baixa `ifo-transacoes-AAAA-MM.csv`, abre no Excel com acentos corretos, separador `;` e vírgula decimal.
6. Tema escuro, celular (tela estreita), sair e tentar abrir uma tela protegida (volta ao login).
7. Cadastrar uma segunda conta e confirmar que ela não vê os dados da primeira.
8. Deixar o site parado mais de 20 minutos e entrar de novo: aparece o aviso "servidor acordando" e o login funciona.

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
- **Em produção:** todos os segredos (`JWT_SECRET`, senha do banco) ficam só nos painéis do Render e do Neon; a API aceita chamadas **somente** do site da Vercel (CORS); o banco da nuvem nunca recebeu o `dev-seed.sql`; o tráfego usa HTTPS e erros não mostram stacktrace. A conta de demonstração é pública de propósito.
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
| No site publicado, o login demora ou aparece "servidor acordando" | O Render gratuito dorme sem uso. Espere até 1 minuto na primeira visita |
| No site publicado, o login falha mesmo esperando (o servidor responde `403 Invalid CORS request`) | O `CORS_ORIGINS` no Render não é o endereço exato do site (sem `/` no final). Corrija e espere o serviço ficar *Live* |
| `COPY src ./src ... not found` no build do Render | O *Docker Build Context Directory* não é `ifo/backend/ifo-backend` |

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

**Equipe de desenvolvimento**

- João Marcos Vilaça Marques

**Equipe de apresentação**

- João Marcos Vilaça Marques
- Amanda Freitas de Lima
- Lucas de Souza Saldanha dos Santos
- Vinicius de Amorim Farias

**Uso de IA**

O projeto foi desenvolvido por João Marcos com o apoio do **Claude Code** (Anthropic) para escrever código, testes e documentação. As decisões de escopo e de arquitetura, a revisão das entregas e todo o controle do Git (commits, pull requests e merges) foram feitos pelo desenvolvedor. As contribuições do Claude Code aparecem no histórico do Git como coautoria (`Co-Authored-By`).

## Próximos passos (IFO V2)

A V2 **não** terá integração bancária, aplicativo mobile nem sugestões com IA. As principais diferenças em relação a esta versão são duas, mais a troca de tecnologias:

- **Autenticação em dois fatores (2FA):** depois da senha, o usuário recebe um código de 6 dígitos por e-mail (válido por 10 minutos, de uso único).
- **Dashboard interativo:** gráficos com Chart.js (pizza dos gastos, linha da evolução do saldo e barras de receitas × despesas por mês).

| Camada | V1 revisada (esta versão) | V2 |
|---|---|---|
| Backend | Java 17 + Spring Boot | Node.js + Express, em MVC |
| Banco relacional | PostgreSQL | MySQL (usuários e códigos 2FA) |
| Banco NoSQL | não havia | MongoDB (transações, categorias, orçamentos, metas, histórico e análises) |
| Frontend | HTML, CSS e JS puro | React (Vite) + Tailwind CSS |
| Gráficos | pizza simples (CSS) | Chart.js |
| Autenticação | BCrypt + JWT | JWT em cookie httpOnly + 2FA por e-mail (Resend) |
| Design | sem protótipo | Figma antes de codar cada tela |
| Acesso a dados | JPA/Hibernate | Sequelize (MySQL) e Mongoose (MongoDB) |
