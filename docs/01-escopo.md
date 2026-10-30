# 01 · Escopo do Projeto

**Projeto:** IFO — Organizador Financeiro Inteligente (V1 revisada: correção e refatoração da V1)
**Equipe:** João Marcos Vilaça Marques, Amanda Freitas de Lima, Lucas de Souza Saldanha dos Santos, Vinicius de Amorim Farias
**Instituição:** FATEC Cotia — Desenvolvimento de Software Multiplataforma (Engenharia de Software II)

---

## 1. Visão

O IFO é uma aplicação web que ajuda pessoas a controlar as finanças pessoais de forma simples: registrar receitas e despesas, seguir a regra 50/30/20, acompanhar metas e simular investimentos.

**Problema:** grande parte da população tem dificuldade para controlar gastos, o que leva a endividamento e falta de planejamento. Ferramentas existentes costumam ser complexas demais para quem está começando.

**Proposta:** um sistema leve, em português, com poucas telas e regras claras, que orienta o usuário pela divisão da renda em **Necessidades (50%)**, **Lazer (30%)** e **Investimentos (20%)** e avisa quando ele estoura um limite.

## 2. Objetivos

**Geral:** entregar um sistema web de gestão financeira pessoal com qualidade de engenharia (arquitetura em camadas, segurança, testes e documentação).

**Específicos**

1. Corrigir os problemas estruturais da V1 (segurança, inconsistência entre banco e código, regras faltantes).
2. Reduzir o escopo ao que realmente é usado (remover Conta/Carteira e persistência de Investimento).
3. Implementar o modelo **Grupo → Categoria (fixa) → Descrição (livre)** nas transações.
4. Permitir que o usuário **defina o próprio orçamento** (padrão 50/30/20, editável) e receba **alerta simples de estouro**.
5. Entregar interface **responsiva (mobile-first)** com design renovado.
6. Manter a documentação (UML, BPMN, requisitos) sincronizada com o código.

## 3. Dentro do escopo (V1 revisada)

| # | Funcionalidade | Resumo |
|---|---|---|
| 1 | Cadastro e login | Autenticação segura (BCrypt + JWT) |
| 2 | Perfil | Editar nome e renda mensal |
| 3 | Transações | CRUD de receitas e despesas com categoria fixa + descrição livre |
| 4 | Filtro por mês | Listagem e totais por mês/ano |
| 5 | Resumo financeiro | Saldo, total de receitas e total de despesas do mês |
| 6 | Orçamento 50/30/20 | Percentuais por grupo, editáveis pelo usuário (soma = 100%) |
| 7 | Alerta de estouro | Aviso simples em 80% (atenção) e acima de 100% (estouro) |
| 8 | Metas financeiras | CRUD, aporte manual e barra de progresso |
| 9 | Simulador de investimentos | Juros simples e compostos, **sem salvar** no banco |
| 10 | Gráfico de pizza simples | Distribuição das despesas do mês por grupo |
| 11 | Despesas recorrentes | Modelo mensal (ex.: internet, luz) que gera a transação automaticamente |
| 12 | Exportar CSV | Download das transações do mês filtrado |
| 13 | Dark mode | Tema claro/escuro alternável, com padrão do sistema |

## 4. Fora do escopo (decisões já tomadas)

- **Conta/Carteira** (tabela, API e diagramas) — removida; não será usada.
- **Persistência de simulações de investimento** — o simulador é apenas cálculo.
- **Categorias criadas pelo usuário** — a lista de categorias é fixa; o usuário altera apenas a **descrição** da transação.
- **Dashboards interativos** (gráficos dinâmicos, cartões avançados, drill-down) — ficam para o **IFO V2 (futuro)**.
- Integração bancária, app mobile nativo, 2FA e IA para sugestões — backlog futuro.

## 5. Decisões recentes de escopo

Em **18/10/2026** a equipe aprovou a inclusão de **despesas recorrentes**, **exportar CSV** e **dark mode** (prioridade *Should*: entram depois dos itens *Must*). Os detalhes estão em `02-requisitos.md` (RF20–RF22, RN14–RN19) e no roadmap (`07-roadmap.md`).

> Regra: qualquer item novo só entra na sprint com decisão registrada em `docs/` (ver ADRs no fim do `07-roadmap.md`).

## 6. Premissas e restrições

- Usuário único por conta (sem compartilhamento entre pessoas).
- Moeda: Real (BRL). Datas no formato brasileiro na interface.
- Stack mantida: Java 17 + Spring Boot 3.2, PostgreSQL 15, HTML/CSS/JavaScript puro, Docker para o banco.
- Equipe de 4 pessoas, sprints semanais, prazo acadêmico.
- O frontend **não** usa frameworks pesados (decisão da V1 mantida para leveza e controle).

## 7. Partes interessadas

| Papel | Quem | Interesse |
|---|---|---|
| Usuário final | Pessoas que querem organizar o dinheiro | Simplicidade e clareza |
| Equipe de desenvolvimento | 4 alunos | Aprendizado e entrega |
| Professor(a) de Engenharia de Software II | FATEC Cotia | Aplicação de métodos e documentação |

## 8. Critérios de sucesso

- Todos os requisitos "Must" de `02-requisitos.md` implementados e testados.
- Nenhuma senha em texto puro; nenhum endpoint expõe dados de outro usuário.
- Banco criado do zero apenas por migrations (sem scripts manuais) e aplicação sobe com um comando.
- Telas utilizáveis de 360 px (celular) a desktop.
- Documentação e diagramas coerentes com o código entregue.

## 9. Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Divergência código × documentação (como na V1) | Alto | Diagramas em Mermaid dentro do repo, revisados no Definition of Done |
| Escopo crescer (itens do IFO V2 invadirem a V1 revisada) | Médio | Lista "fora do escopo" + ADRs |
| Tempo curto | Médio | Priorização MoSCoW e sprints curtas |
| Problemas de ambiente (Docker/CORS) | Baixo | `docker-compose` + variáveis de ambiente documentadas |
