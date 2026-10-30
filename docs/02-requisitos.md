# 02 · Requisitos e Regras de Negócio

Prioridade segue MoSCoW: **M** (Must), **S** (Should), **C** (Could).

## 1. Requisitos funcionais

### Conta do usuário

| ID | Requisito | Prior. |
|---|---|---|
| RF01 | O usuário se cadastra informando nome, e-mail, senha e renda mensal. | M |
| RF02 | O usuário faz login com e-mail e senha e recebe um token JWT. | M |
| RF03 | O usuário encerra a sessão (logout) no cliente. | M |
| RF04 | O usuário edita nome e renda mensal. | M |

### Transações

| ID | Requisito | Prior. |
|---|---|---|
| RF05 | O usuário cadastra transação informando valor, data, **categoria** (lista fixa) e **descrição** livre. O tipo (receita/despesa) e o grupo são derivados da categoria. | M |
| RF06 | O usuário lista, edita e exclui suas transações. A edição é um `PUT` (não "apagar e recriar"). | M |
| RF07 | O usuário filtra transações e totais por **mês/ano**. | M |
| RF08 | O sistema exibe o resumo do mês: total de receitas, total de despesas e saldo. | M |
| RF09 | O sistema lista as categorias disponíveis (somente leitura), agrupadas. | M |
| RF10 | O sistema exibe **gráfico de pizza simples** das despesas do mês por grupo. | S |

### Orçamento

| ID | Requisito | Prior. |
|---|---|---|
| RF11 | Ao se cadastrar, o usuário recebe o orçamento padrão 50/30/20. | M |
| RF12 | O usuário edita os percentuais de Necessidades, Lazer e Investimentos; a soma deve ser 100%. | M |
| RF13 | O sistema calcula, por grupo, limite (R$), gasto no mês, % usado e nível (OK / ATENÇÃO / ESTOURO). | M |
| RF14 | Ao lançar uma despesa, o sistema avisa se o grupo entrou em ATENÇÃO ou ESTOURO. O aviso também aparece no dashboard. | M |

### Metas

| ID | Requisito | Prior. |
|---|---|---|
| RF15 | O usuário cria, lista, edita e exclui metas (título, valor objetivo, prazo). | M |
| RF16 | O usuário registra aportes na meta (atualiza o valor atual). | M |
| RF17 | O sistema exibe barra de progresso (%) e marca a meta como concluída ao atingir o objetivo. | M |

### Simulador de investimentos

| ID | Requisito | Prior. |
|---|---|---|
| RF18 | O usuário informa aplicação inicial (opcional), aporte mensal, taxa mensal (%), prazo (meses) e tipo de juros (simples/composto) e recebe montante final, total investido e rendimento. | M |
| RF19 | O resultado traz a evolução mês a mês em tabela. **Nada é salvo no banco.** | S |

### Recursos complementares (aprovados em 18/10/2026)

| ID | Requisito | Prior. |
|---|---|---|
| RF20 | **Despesas recorrentes:** o usuário cadastra, lista, edita, pausa e exclui um modelo mensal (categoria de despesa, valor, descrição, dia do mês, início e fim opcional). O sistema gera a transação automaticamente a cada mês. | S |
| RF21 | **Exportar CSV:** o usuário baixa as transações do mês/ano filtrado em arquivo CSV. | S |
| RF22 | **Dark mode:** o usuário alterna entre tema claro e escuro; o padrão segue o sistema e a escolha fica salva no navegador. | S |

## 2. Requisitos não funcionais

| ID | Categoria | Requisito |
|---|---|---|
| RNF01 | Segurança | Senhas com **BCrypt**; autenticação **JWT** stateless; a API nunca devolve a senha/hash. |
| RNF02 | Segurança | O identificador do usuário vem do **token**, nunca de parâmetro de URL (`?usuario=`). Cada usuário só acessa os próprios dados. |
| RNF03 | Segurança | Entrada validada (Bean Validation) e saída escapada no frontend (sem `innerHTML` com dado do usuário). |
| RNF04 | Segurança | Segredos (senha do banco, chave JWT) por **variáveis de ambiente**, nunca no repositório. CORS restrito à origem do frontend. |
| RNF05 | Desempenho | Listagens respondem em menos de 2 s para 1.000 transações do usuário. |
| RNF06 | Usabilidade | Fluxo "lançar despesa" em no máximo 4 interações; mensagens de erro claras em português. |
| RNF07 | Responsividade | Layout **mobile-first**, funcional a partir de 360 px de largura até desktop. |
| RNF08 | Acessibilidade | Contraste mínimo AA, campos com `label`, navegação por teclado, foco visível. |
| RNF09 | Manutenibilidade | Camadas Controller → Service → Repository; **DTOs** na API; entidades não expostas. |
| RNF10 | Manutenibilidade | Esquema do banco versionado por **Flyway**; `ddl-auto=validate`. |
| RNF11 | Confiabilidade | Valores monetários em `BigDecimal`/`NUMERIC(12,2)`; erros tratados por `@ControllerAdvice` com formato único. |
| RNF12 | Testabilidade | Testes unitários de serviços e BDD (Cucumber) chamando o código real; banco de teste isolado (Testcontainers). |
| RNF13 | Portabilidade | `docker-compose` sobe banco (e opcionalmente backend); README com passo a passo. |
| RNF14 | Compatibilidade | Chrome, Edge, Firefox e Safari atuais. |
| RNF15 | Segurança | A exportação CSV usa apenas dados do usuário do token e neutraliza injeção de fórmulas (RN18). |
| RNF16 | Acessibilidade | Os dois temas (claro e escuro) mantêm contraste AA; cores definidas só por variáveis CSS. |
| RNF17 | Confiabilidade | A geração de recorrências é idempotente: rodar o job duas vezes não duplica transações (RN15). |

## 3. Regras de negócio

| ID | Regra |
|---|---|
| RN01 | O valor de uma transação é sempre **> 0**; o sinal vem do tipo (receita soma, despesa subtrai). |
| RN02 | O **tipo** (RECEITA/DESPESA) e o **grupo** (NECESSIDADES/LAZER/INVESTIMENTOS) da transação são **derivados da categoria**. O cliente envia apenas a categoria. |
| RN03 | Receitas não pertencem a grupo de orçamento. |
| RN04 | **Saldo = Σ receitas − Σ despesas.** Aportes em Investimentos contam como despesa (saem do saldo disponível). |
| RN05 | Orçamento: os três percentuais são ≥ 0 e **somam exatamente 100%**. Padrão: 50 / 30 / 20. |
| RN06 | **Limite do grupo = renda mensal × percentual.** Se a renda for 0, nenhum limite é calculado e o aviso não é exibido (o app pede para informar a renda). |
| RN07 | Gasto do grupo = Σ despesas do mês/ano no grupo. **% usado = gasto ÷ limite.** Níveis: **OK** < 80%, **ATENÇÃO** de 80% a 100%, **ESTOURO** > 100%. |
| RN08 | O alerta é informativo: **nunca bloqueia** o lançamento da despesa. |
| RN09 | Meta: progresso = valor atual ÷ valor objetivo (exibido limitado a 100%). Concluída quando valor atual ≥ objetivo. Objetivo > 0; valor atual ≥ 0. |
| RN10 | E-mail único; senha com no mínimo 8 caracteres; renda mensal ≥ 0. |
| RN11 | Um usuário só lê, altera ou exclui dados que lhe pertencem (transações e metas). |
| RN12 | Categorias são **fixas** (carregadas por migration) e iguais para todos os usuários. |

### Fórmulas do simulador (RN13)

Seja `P` a aplicação inicial, `PMT` o aporte mensal (feito no **fim** de cada mês), `i` a taxa mensal em decimal (`taxa% ÷ 100`) e `n` o prazo em meses.

- **Composto:** `M = P·(1+i)^n + PMT · ((1+i)^n − 1) / i` (se `i = 0`, `M = P + PMT·n`)
- **Simples:** `M = P·(1 + i·n) + PMT·n + PMT·i·n·(n−1)/2`
- **Total investido** = `P + PMT·n`; **rendimento** = `M − total investido`.

Conferência (exemplo da V1): `P=0`, `PMT=300`, `i=1%`, `n=12`, composto → **R$ 3.804,75**.

Arredondamento: `HALF_EVEN`/`HALF_UP` com 2 casas apenas no resultado final.

### Regras de recorrência, exportação e tema

| ID | Regra |
|---|---|
| RN14 | Só categorias do tipo **DESPESA** podem ser recorrentes. O dia do mês vai de 1 a 31; em meses mais curtos vale o **último dia** do mês. |
| RN15 | Um job diário (`@Scheduled`) gera as ocorrências cuja data já chegou. Cada recorrência guarda a **última competência gerada** (mês); a geração avança mês a mês até o mês atual, inclusive meses perdidos, sem duplicar. Ao criar uma recorrência, as ocorrências já vencidas são geradas na hora. |
| RN16 | Editar, pausar ou excluir uma recorrência afeta **somente meses futuros**. Transações já geradas permanecem, são transações comuns (editáveis e excluíveis) e continuam contando no orçamento. Excluir a recorrência mantém as transações geradas (vínculo vira nulo). |
| RN17 | Recorrência só gera dentro do período (`data_inicio` ≤ ocorrência ≤ `data_fim`, se houver) e enquanto estiver **ativa**. |
| RN18 | **CSV:** colunas `Data;Tipo;Grupo;Categoria;Descrição;Valor`; separador `;`; valor com vírgula decimal e sem símbolo de moeda; datas `dd/MM/yyyy`; codificação UTF-8 **com BOM** (abre certo no Excel). Textos iniciados por `=`, `+`, `-` ou `@` recebem apóstrofo na frente. Exporta só as transações do usuário no mês/ano pedido. |
| RN19 | **Tema:** sem escolha do usuário, vale o `prefers-color-scheme` do sistema. A escolha manual é salva em `localStorage` e prevalece. |

## 4. Categorias fixas

| Tipo | Grupo | Categorias |
|---|---|---|
| RECEITA | — | Salário · Freelance · Rendimentos · Mesada/Presente · Reembolso · Outras receitas |
| DESPESA | NECESSIDADES | Moradia · Contas fixas · Alimentação · Transporte · Saúde · Educação · Parcelas e dívidas |
| DESPESA | LAZER | Delivery · Passeio · Assinaturas · Compras e vestuário · Viagem · Outros (lazer) |
| DESPESA | INVESTIMENTOS | Renda fixa · Renda variável · Reserva de emergência · Previdência |

Exemplos de uso (campo **descrição** livre):

- *Delivery* → "pizza"
- *Contas fixas* → "internet", "luz"
- *Assinaturas* → "streaming"

## 5. Rastreabilidade resumida

| Funcionalidade | Requisitos | Regras |
|---|---|---|
| Cadastro/Login | RF01–RF04, RNF01–RNF04 | RN10, RN11 |
| Transações | RF05–RF10 | RN01–RN04, RN12 |
| Orçamento e alerta | RF11–RF14 | RN05–RN08 |
| Metas | RF15–RF17 | RN09 |
| Simulador | RF18–RF19 | RN13 |
| Recorrências | RF20 | RN14–RN17 |
| Exportar CSV | RF21 | RN18 |
| Dark mode | RF22 | RN19 |
