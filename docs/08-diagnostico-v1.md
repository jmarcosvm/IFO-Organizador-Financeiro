# 08 · Diagnóstico da V1

Resultado da leitura do repositório `jmarcosvm/IFO-Organizador-Financeiro` (commit "Entrega final IFO") e do PDF do seminário. Serve de ponto de partida da refatoração. Cada item aponta para a sprint em que será resolvido.

## 1. Segurança

| # | Achado | Onde | Sprint |
|---|---|---|---|
| S1 | **Senha em texto puro**: o login compara `String.equals` e o cadastro grava a senha como veio. | `UsuarioService` | R1 |
| S2 | `POST /usuarios` e `/usuarios/login` **devolvem o `Usuario` inteiro, com a senha**. | `UsuarioController` | R1 |
| S3 | **Sem autenticação/autorização**: qualquer pessoa lê, cria ou apaga dados de outro usuário mudando `?usuario=ID` ou o `idUsuario` do corpo. | todos os controllers | R1 |
| S4 | O PDF e o BPMN citam **BCrypt e JWT**, mas o `pom.xml` não tem Spring Security. | `pom.xml` | R1 |
| S5 | `@CrossOrigin(origins = "*")` em todos os controllers. | controllers | R1 |
| S6 | Credenciais do banco (`postgres` / `password`) no repositório público. | `application.properties` | R1 |
| S7 | **XSS**: `innerHTML` com `descricao` e outros campos sem escape. | `app.js` | R4 |
| S8 | Sem validação de entrada (`@Valid`), aceitando valor negativo/zero e campos nulos. | controllers/models | R1–R2 |
| S9 | Usuário "logado" é um objeto em `localStorage`, sem token. | `app.js` | R1/R4 |

## 2. Banco de dados e modelo

| # | Achado | Sprint |
|---|---|---|
| B1 | `Categoria` (entidade) usa `tipo` enum único + `descricao`; a tabela tem `nome_categoria`, `id_usuario` e `tipo` RECEITA/DESPESA. **Não batem.** | R1 |
| B2 | `OrcamentoMensal` usa `mes` e `ano`; a tabela mantém `mes_referencia NOT NULL` e não tem coluna `mes`. O insert deve falhar. | R1 |
| B3 | O script SQL termina com `ALTER COLUMN categoria DROP NOT NULL` **depois** de dropar a coluna `categoria` (erro), e tem `DELETE FROM orcamento_mensal` solto. | R1 |
| B4 | `ddl-auto=none` + ajustes manuais por `ALTER TABLE` no meio do script: banco não é reproduzível. | R1 |
| B5 | Enums do banco convertidos para `VARCHAR` com `ALTER` após criar (resquício de tentativa-e-erro). | R1 |
| B6 | `Transacao` tem `categoriaTipo` redundante e `idUsuario`/`idConta`/`idCategoria` como `Long` solto (sem relacionamento JPA). | R1 |
| B7 | Tabela e entidade de **Conta/Carteira** e de **Investimento** existem, mas não serão usadas. | R1 |
| B8 | Dados de teste (usuário "Douglas") misturados ao esquema, com hash de senha inválido (`$2y$10$92IXUNpkjO0rOQ5byMi`, truncado). | R1/R5 |

## 3. Regras de negócio

| # | Achado | Sprint |
|---|---|---|
| N1 | A regra 50/30/20 é só `renda × %` no frontend; **não compara com os gastos** e não há alerta de estouro (previsto no BPMN). | R3 |
| N2 | Orçamento padrão é **criado pelo frontend** ao abrir a tela vazia (3 `POST` disparados pelo `app.js`), e não pelo backend no cadastro. | R3 |
| N3 | O usuário **não consegue ajustar os percentuais**. | R3 |
| N4 | A simulação de investimento é calculada só no JS; o backend apenas grava o `montanteFinal` recebido. | R3 |
| N5 | O BPMN prevê "verificar saldo suficiente" antes da despesa; não existe. | descartado (RN08: alerta não bloqueia) |
| N6 | Categoria e grupo hoje são a mesma coisa (enum de 8 valores). Falta o modelo Grupo → Categoria → Descrição. | R2 |
| N7 | O saldo ignora filtro de período (soma tudo desde sempre). | R2 |
| N8 | Metas: existe `atualizarValor` no service, mas nenhum endpoint o expõe (sem aporte pelo backend). | R3 |

## 4. API e backend

| # | Achado | Sprint |
|---|---|---|
| A1 | Editar transação no frontend é **DELETE + POST**: perde o id, não é atômico. | R2 |
| A2 | `DELETE /transacoes/{id}` não verifica dono nem existência. | R2 |
| A3 | Exceções genéricas (`RuntimeException`) e `orElseThrow()` sem mensagem; sem tratamento central. | R1 |
| A4 | Entidades JPA expostas direto na API. | R1 |
| A5 | Injeção por `@Autowired` em campo (preferir construtor). | R1 |
| A6 | `ContaController` só lista e cria; faltam editar/excluir, e não há tela. (Será removido.) | R1 |
| A7 | `inicializarCategoriasPadrao` faz `findAll()` dentro de loop. (Será substituído por migration.) | R1 |

## 5. Frontend

| # | Achado | Sprint |
|---|---|---|
| F1 | `app.js` com ~800 linhas, tudo global, misturando API, regra e HTML. | R4 |
| F2 | Dashboard calcula receitas/despesas **no navegador** (faz 2 requisições: saldo e lista completa) e sem filtro de mês. | R2/R4 |
| F3 | `orcamentos.html` tem dois elementos com o mesmo `id="lista-orcamentos"` (HTML inválido). | R4 |
| F4 | Nome "Douglas" fixo no HTML até o JS rodar; rodapé "Sprint 3". | R4 |
| F5 | `API_URL` fixa em `localhost:8081`. | R4 |
| F6 | Design e responsividade a melhorar; **bugs visuais a levantar** (checklist em `07-roadmap.md` §3). | R4 |

## 6. Testes

| # | Achado | Sprint |
|---|---|---|
| T1 | Os steps de login **não chamam o `UsuarioService`**: comparam com a senha fixa `"123456"`. O teste passa sem testar nada. | R5 |
| T2 | Testes de transação gravam no banco local real (precisam do Postgres no ar) e deixam lixo. | R5 |
| T3 | `IfoBackendApplicationTests` e Cucumber dependem de banco externo. | R5 |
| T4 | `surefire` configurado para incluir `**/*Steps.java` (classes de steps não são testes). | R5 |

## 7. Divergências entre o PDF do seminário e o código

| PDF / diagramas | Código atual | Ação |
|---|---|---|
| Hash BCrypt e token JWT no processo de autenticação | Nada disso existe | Implementar (R1) |
| Diagramas com Conta/Carteira, "Gerenciar Contas" e "Gerenciar Categorias (CRUD)" | Contas sem tela; categorias fixas | Remover do escopo e dos diagramas (feito em `05-diagramas.md`) |
| Alerta de estouro e "Verificar saldo" no BPMN | Inexistentes | Alerta em R3; verificação descartada |
| Simulação com "Salvar simulação no BD" | Grava no banco um `montanteFinal` calculado no JavaScript | Calculadora sem persistência |
| `TransacaoReceita` e `TransacaoDespesa` no diagrama de classes | Uma entidade `Transacao` só | Diagrama novo usa `Transacao` única |
| Usuário "Douglas" nos diagramas | — | Trocado por "João" |
| Referência "FAWLER, Martin" | — | Corrigir para **FOWLER** |
| Resultado "totalmente funcional" e "atende a todos os requisitos" | Vários requisitos parciais | Reescrever resultados após a V2 |
