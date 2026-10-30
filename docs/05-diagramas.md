# 05 · Diagramas

Todos os diagramas estão em **Mermaid** (renderizam direto no GitHub e no VS Code com extensão). O usuário de exemplo é **João**. Eles substituem os diagramas da V1 e refletem o escopo da V1 revisada (sem Conta/Carteira, sem persistência de investimento).

## 1. Diagrama de casos de uso

Todos os casos de uso abaixo fazem parte do sistema **IFO**; o único ator é o usuário (João).

```mermaid
flowchart LR
    ator(["Usuário<br/>(João)"])

    uc1(["Cadastrar-se"])
    uc2(["Realizar login"])
    uc3(["Editar perfil e renda"])
    uc4(["Registrar transação<br/>(categoria + descrição)"])
    uc5(["Editar ou excluir transação"])
    uc6(["Consultar transações por mês"])
    uc7(["Ver resumo do mês<br/>e gráfico de pizza"])
    uc8(["Configurar orçamento<br/>(50/30/20 editável)"])
    uc9(["Exibir alerta de estouro"])
    uc10(["Gerenciar metas"])
    uc11(["Registrar aporte na meta"])
    uc12(["Simular investimento<br/>(simulador)"])
    uc13(["Gerenciar despesas<br/>recorrentes"])
    uc14(["Exportar transações<br/>em CSV"])
    uc15(["Alternar tema<br/>claro/escuro"])
    uc16(["Gerar transações<br/>recorrentes"])
    sched(["Agendador<br/>do sistema"])

    ator --- uc1
    ator --- uc2
    ator --- uc3
    ator --- uc4
    ator --- uc5
    ator --- uc6
    ator --- uc7
    ator --- uc8
    ator --- uc10
    ator --- uc12
    ator --- uc13
    ator --- uc14
    ator --- uc15
    sched --- uc16

    uc4 -. "«include»" .-> uc9
    uc8 -. "«include»" .-> uc9
    uc11 -. "«extend»" .-> uc10
    uc16 -. "«extend»" .-> uc13
```

## 2. Diagrama de classes

```mermaid
classDiagram
    direction LR

    class Usuario {
        -Long idUsuario
        -String nome
        -String email
        -String senhaHash
        -BigDecimal rendaMensal
        -LocalDateTime dataCriacao
    }

    class Categoria {
        -Short idCategoria
        -String nome
        -TipoCategoria tipo
        -GrupoOrcamento grupo
    }

    class Transacao {
        -Long idTransacao
        -BigDecimal valor
        -LocalDate data
        -String descricao
        -LocalDateTime criadoEm
    }

    class Orcamento {
        -Long idOrcamento
        -GrupoOrcamento grupo
        -BigDecimal percentual
    }

    class MetaFinanceira {
        -Long idMeta
        -String tituloMeta
        -BigDecimal valorObjetivo
        -BigDecimal valorAtual
        -LocalDate prazo
        +calcularProgresso() BigDecimal
        +estaConcluida() boolean
    }

    class TransacaoRecorrente {
        -Long idRecorrencia
        -BigDecimal valor
        -String descricao
        -Integer diaMes
        -LocalDate dataInicio
        -LocalDate dataFim
        -Boolean ativa
        -LocalDate ultimaCompetencia
    }

    class TipoCategoria {
        <<enumeration>>
        RECEITA
        DESPESA
    }

    class GrupoOrcamento {
        <<enumeration>>
        NECESSIDADES
        LAZER
        INVESTIMENTOS
    }

    class TipoJuros {
        <<enumeration>>
        SIMPLES
        COMPOSTO
    }

    class TransacaoService {
        +criar(usuarioId, dto)
        +listarPorMes(usuarioId, mes, ano)
        +atualizar(usuarioId, id, dto)
        +excluir(usuarioId, id)
        +resumoMensal(usuarioId, mes, ano)
    }

    class OrcamentoService {
        +obter(usuarioId)
        +atualizar(usuarioId, percentuais)
        +status(usuarioId, mes, ano)
    }

    class SimuladorService {
        +simular(entrada)
    }

    class RecorrenciaService {
        +criar(usuarioId, dto)
        +gerarPendentes()
    }

    class ExportacaoService {
        +exportarCsv(usuarioId, mes, ano)
    }

    Usuario "1" --> "0..*" Transacao : registra
    Usuario "1" --> "0..*" TransacaoRecorrente : programa
    Categoria "1" --> "0..*" TransacaoRecorrente : classifica
    TransacaoRecorrente "1" --> "0..*" Transacao : gera
    RecorrenciaService ..> TransacaoRecorrente
    ExportacaoService ..> Transacao
    Usuario "1" --> "3" Orcamento : configura
    Usuario "1" --> "0..*" MetaFinanceira : define
    Categoria "1" --> "0..*" Transacao : classifica
    Categoria ..> TipoCategoria
    Categoria ..> GrupoOrcamento
    Orcamento ..> GrupoOrcamento
    TransacaoService ..> OrcamentoService : consulta status
    SimuladorService ..> TipoJuros
```

> `ResumoMensal`, `ResultadoSimulacao`, `StatusOrcamento` e `TransacaoResposta` são **DTOs** (não são tabelas). Não existe classe de conta/carteira nem de investimento persistido.

## 3. Diagramas de sequência

### 3.1 Login

```mermaid
sequenceDiagram
    autonumber
    actor J as João
    participant FE as Frontend
    participant API as AuthController
    participant SVC as UsuarioService
    participant DB as PostgreSQL

    J->>FE: Informa e-mail e senha
    FE->>API: POST /api/auth/login
    API->>SVC: autenticar(email, senha)
    SVC->>DB: SELECT usuario WHERE email
    DB-->>SVC: usuário (senha_hash)
    SVC->>SVC: BCrypt.matches(senha, hash)
    alt Credenciais válidas
        SVC-->>API: usuário autenticado
        API-->>FE: 200 { token JWT, nome, rendaMensal }
        FE->>FE: Guarda o token
        FE-->>J: Abre o dashboard
    else Credenciais inválidas
        API-->>FE: 401 { mensagem }
        FE-->>J: "E-mail ou senha inválidos"
    end
```

### 3.2 Registrar despesa com alerta de orçamento

```mermaid
sequenceDiagram
    autonumber
    actor J as João
    participant FE as Frontend
    participant TC as TransacaoController
    participant TS as TransacaoService
    participant OS as OrcamentoService
    participant DB as PostgreSQL

    J->>FE: Escolhe categoria "Delivery", valor, data e descrição "pizza"
    FE->>TC: POST /api/transacoes (Authorization: Bearer token)
    TC->>TC: Extrai usuarioId do token
    TC->>TS: criar(usuarioId, dto)
    TS->>DB: Busca categoria (tipo e grupo)
    DB-->>TS: Delivery · DESPESA · LAZER
    TS->>TS: Valida valor maior que zero
    TS->>DB: INSERT transacao
    TS->>OS: status(usuarioId, mês, ano)
    OS->>DB: Soma despesas do mês por grupo
    OS->>DB: Busca renda e percentuais
    OS-->>TS: Lazer: 104% usado (ESTOURO)
    TS-->>TC: transação + alerta
    TC-->>FE: 201 { transacao, alerta }
    FE-->>J: Mostra confirmação e aviso "Você estourou o limite de Lazer"
```

### 3.3 Simular investimento (sem persistência)

```mermaid
sequenceDiagram
    autonumber
    actor J as João
    participant FE as Frontend
    participant IC as InvestimentoController
    participant SS as SimuladorService

    J->>FE: Informa aporte, taxa, prazo e tipo de juros
    FE->>IC: POST /api/investimentos/simular
    IC->>SS: simular(entrada)
    SS->>SS: Aplica fórmula (simples ou composto)
    SS-->>IC: montante, total investido, rendimento, evolução
    IC-->>FE: 200 { resultado }
    FE-->>J: Exibe resultado e tabela mês a mês
    Note over IC,SS: Nada é gravado no banco
```

## 4. Diagramas de processo (BPMN simplificado)

Os processos seguem a legenda da V1: **cor** indica quem executa a tarefa. Formas: retângulo = tarefa, losango = decisão, círculo = início/fim, cilindro = banco de dados.

| Cor | Significado |
|---|---|
| Azul | Tarefa do usuário / frontend |
| Verde | Tarefa do backend (processamento) |
| Roxo | Persistência no banco de dados |
| Vermelho | Mensagem de alerta ou erro |
| Amarelo | Decisão (gateway exclusivo) |

### 4.1 Processo 1 — Autenticação e cadastro

```mermaid
flowchart TD
    i1(("Acessar<br/>sistema")):::ini --> g1{"Possui conta?"}:::gw

    g1 -- "Não" --> u1["Preencher cadastro"]:::usr
    u1 --> f1["Validar formulário"]:::usr
    f1 --> b1["Gerar hash BCrypt e<br/>validar e-mail único"]:::be
    b1 --> d1[("Salvar usuário")]:::db
    d1 --> b2["Criar orçamento<br/>padrão 50/30/20"]:::be
    b2 --> d2[("Salvar orçamento")]:::db
    d2 --> e1(("Cadastro<br/>concluído")):::fim

    g1 -- "Sim" --> u2["Informar e-mail e senha"]:::usr
    u2 --> d3[("Buscar usuário")]:::db
    d3 --> g2{"Credenciais<br/>válidas?"}:::gw
    g2 -- "Sim" --> b3["Gerar token JWT"]:::be
    b3 --> f2["Guardar token e<br/>abrir dashboard"]:::usr
    f2 --> e2(("Login<br/>realizado")):::fim
    g2 -- "Não" --> r1["Exibir erro de<br/>autenticação"]:::alert
    r1 --> u2

    classDef usr fill:#DBEAFE,stroke:#2563EB,color:#111;
    classDef be fill:#DCFCE7,stroke:#16A34A,color:#111;
    classDef db fill:#EDE9FE,stroke:#7C3AED,color:#111;
    classDef alert fill:#FEE2E2,stroke:#DC2626,color:#111;
    classDef gw fill:#FEF9C3,stroke:#CA8A04,color:#111;
    classDef ini fill:#BBF7D0,stroke:#16A34A,color:#111;
    classDef fim fill:#FECACA,stroke:#DC2626,color:#111;
```

### 4.2 Processo 2 — Registrar transação e alertar estouro

```mermaid
flowchart TD
    i2(("Nova<br/>transação")):::ini --> u1["Escolher categoria, valor,<br/>data e descrição"]:::usr
    u1 --> f1["Validar campos"]:::usr
    f1 --> b1["Derivar tipo e grupo<br/>da categoria"]:::be
    b1 --> g1{"Valor maior<br/>que zero?"}:::gw
    g1 -- "Não" --> r1["Exibir erro<br/>de validação"]:::alert
    r1 --> e2(("Não<br/>registrada")):::fim
    g1 -- "Sim" --> d1[("Salvar transação")]:::db
    d1 --> g2{"É despesa?"}:::gw
    g2 -- "Não" --> f2["Exibir confirmação"]:::usr
    g2 -- "Sim" --> d2[("Somar despesas<br/>do grupo no mês")]:::db
    d2 --> b2["Calcular % usado<br/>do limite do grupo"]:::be
    b2 --> g3{"Nível do grupo"}:::gw
    g3 -- "OK (menos de 80%)" --> f2
    g3 -- "ATENÇÃO ou ESTOURO" --> r2["Exibir alerta<br/>de orçamento"]:::alert
    r2 --> f2
    f2 --> e1(("Transação<br/>registrada")):::fim

    classDef usr fill:#DBEAFE,stroke:#2563EB,color:#111;
    classDef be fill:#DCFCE7,stroke:#16A34A,color:#111;
    classDef db fill:#EDE9FE,stroke:#7C3AED,color:#111;
    classDef alert fill:#FEE2E2,stroke:#DC2626,color:#111;
    classDef gw fill:#FEF9C3,stroke:#CA8A04,color:#111;
    classDef ini fill:#BBF7D0,stroke:#16A34A,color:#111;
    classDef fim fill:#FECACA,stroke:#DC2626,color:#111;
```

### 4.3 Processo 3 — Configurar orçamento

```mermaid
flowchart TD
    i3(("Editar<br/>orçamento")):::ini --> u1["Ajustar percentuais<br/>dos 3 grupos"]:::usr
    u1 --> f1["Mostrar soma<br/>em tempo real"]:::usr
    f1 --> g1{"Soma igual<br/>a 100%?"}:::gw
    g1 -- "Não" --> r1["Exibir erro: soma<br/>diferente de 100%"]:::alert
    r1 --> u1
    g1 -- "Sim" --> d1[("Salvar percentuais")]:::db
    d1 --> b1["Calcular limite<br/>renda × percentual"]:::be
    b1 --> f2["Exibir limites em R$"]:::usr
    f2 --> e1(("Orçamento<br/>atualizado")):::fim

    classDef usr fill:#DBEAFE,stroke:#2563EB,color:#111;
    classDef be fill:#DCFCE7,stroke:#16A34A,color:#111;
    classDef db fill:#EDE9FE,stroke:#7C3AED,color:#111;
    classDef alert fill:#FEE2E2,stroke:#DC2626,color:#111;
    classDef gw fill:#FEF9C3,stroke:#CA8A04,color:#111;
    classDef ini fill:#BBF7D0,stroke:#16A34A,color:#111;
    classDef fim fill:#FECACA,stroke:#DC2626,color:#111;
```

### 4.4 Processo 4 — Metas financeiras

```mermaid
flowchart TD
    i4(("Definir<br/>meta")):::ini --> u1["Informar título, valor<br/>objetivo e prazo"]:::usr
    u1 --> g1{"Dados válidos?"}:::gw
    g1 -- "Não" --> r1["Exibir erro<br/>de validação"]:::alert
    r1 --> u1
    g1 -- "Sim" --> d1[("Salvar meta")]:::db
    d1 --> f1["Exibir barra<br/>de progresso"]:::usr
    f1 --> u2["Registrar aporte"]:::usr
    u2 --> b1["Somar aporte ao<br/>valor atual"]:::be
    b1 --> d2[("Atualizar<br/>valor atual")]:::db
    d2 --> g2{"Valor atual maior ou<br/>igual ao objetivo?"}:::gw
    g2 -- "Não" --> f1
    g2 -- "Sim" --> f2["Marcar meta<br/>como concluída"]:::usr
    f2 --> e1(("Meta<br/>concluída")):::fim

    classDef usr fill:#DBEAFE,stroke:#2563EB,color:#111;
    classDef be fill:#DCFCE7,stroke:#16A34A,color:#111;
    classDef db fill:#EDE9FE,stroke:#7C3AED,color:#111;
    classDef alert fill:#FEE2E2,stroke:#DC2626,color:#111;
    classDef gw fill:#FEF9C3,stroke:#CA8A04,color:#111;
    classDef ini fill:#BBF7D0,stroke:#16A34A,color:#111;
    classDef fim fill:#FECACA,stroke:#DC2626,color:#111;
```

### 4.5 Processo 5 — Simulador de investimentos

```mermaid
flowchart TD
    i5(("Simular<br/>investimento")):::ini --> u1["Informar aporte, taxa,<br/>prazo e tipo de juros"]:::usr
    u1 --> g1{"Tipo de juros"}:::gw
    g1 -- "Simples" --> b1["Calcular<br/>juros simples"]:::be
    g1 -- "Composto" --> b2["Calcular<br/>juros compostos"]:::be
    b1 --> b3["Gerar montante, rendimento<br/>e evolução mês a mês"]:::be
    b2 --> b3
    b3 --> f1["Exibir resultado e tabela"]:::usr
    f1 --> e1(("Simulação<br/>concluída")):::fim

    classDef usr fill:#DBEAFE,stroke:#2563EB,color:#111;
    classDef be fill:#DCFCE7,stroke:#16A34A,color:#111;
    classDef gw fill:#FEF9C3,stroke:#CA8A04,color:#111;
    classDef ini fill:#BBF7D0,stroke:#16A34A,color:#111;
    classDef fim fill:#FECACA,stroke:#DC2626,color:#111;
```

> Observação: a V1 tinha uma etapa "Salvar simulação no BD". Ela foi **removida** (decisão ADR-003).

### 4.6 Processo 6 — Geração automática de despesa recorrente

```mermaid
flowchart TD
    i6(("Job diário<br/>(@Scheduled)")):::ini --> d1[("Buscar recorrências ativas<br/>dentro do período")]:::db
    d1 --> g1{"Existe mês pendente<br/>com data até hoje?"}:::gw
    g1 -- "Não" --> e1(("Concluído")):::fim
    g1 -- "Sim" --> b1["Calcular a data do mês<br/>(último dia em meses curtos)"]:::be
    b1 --> d2[("Criar transação vinculada<br/>à recorrência")]:::db
    d2 --> d3[("Atualizar<br/>última competência")]:::db
    d3 --> g1

    classDef be fill:#DCFCE7,stroke:#16A34A,color:#111;
    classDef db fill:#EDE9FE,stroke:#7C3AED,color:#111;
    classDef gw fill:#FEF9C3,stroke:#CA8A04,color:#111;
    classDef ini fill:#BBF7D0,stroke:#16A34A,color:#111;
    classDef fim fill:#FECACA,stroke:#DC2626,color:#111;
```

> A geração é **idempotente**: a última competência impede duplicar transações mesmo que o job rode duas vezes ou fique dias parado (RN15).

## 5. Arquitetura em camadas

```mermaid
flowchart TB
    subgraph cliente ["Cliente (navegador)"]
        ui["HTML + CSS + JavaScript<br/>telas responsivas"]
    end
    subgraph servidor ["Backend — Spring Boot"]
        sec["Spring Security<br/>filtro JWT"]
        ctrl["Controllers REST<br/>(DTOs + validação)"]
        svc["Services<br/>(regras de negócio)"]
        repo["Repositories<br/>(Spring Data JPA)"]
        job["Agendador<br/>(@Scheduled)"]
        erro["@RestControllerAdvice<br/>(tratamento de erros)"]
    end
    subgraph dados ["Dados"]
        fly["Flyway<br/>(migrations)"]
        pg[("PostgreSQL 15")]
    end

    ui -- "JSON + Bearer token" --> sec --> ctrl --> svc --> repo --> pg
    fly --> pg
    job --> svc
    ctrl -.-> erro
```
