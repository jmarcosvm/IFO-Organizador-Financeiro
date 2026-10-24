#language: pt
Funcionalidade: Gestão de Transações Financeiras
  Como usuário do IFO
  Quero gerenciar minhas transações
  Para controlar minhas finanças pessoais

  Contexto:
    Dado que estou autenticado como um novo usuário

  @integracao
  Cenário: Criar transação de receita com sucesso
    Quando eu lanço 1500.00 na categoria "Salário"
    Então a transação deve ser salva com sucesso
    E o tipo derivado da categoria deve ser "RECEITA" sem grupo

  @integracao
  Cenário: Criar transação de despesa com sucesso
    Quando eu lanço 55.90 na categoria "Delivery"
    Então a transação deve ser salva com sucesso
    E o tipo derivado da categoria deve ser "DESPESA" no grupo "LAZER"

  @integracao
  Cenário: Outro usuário não consegue excluir minha transação
    Dado que eu lancei 100.00 na categoria "Delivery"
    Quando outro usuário tenta excluir essa transação
    Então a exclusão deve ser negada com a mensagem "Transação não encontrada."

  @integracao
  Cenário: Editar minha transação mantém o mesmo id
    Dado que eu lancei 50.00 na categoria "Delivery"
    Quando eu edito essa transação para 80.00 na categoria "Alimentação"
    Então a transação deve manter o mesmo id
    E ela deve estar com valor 80.00 na categoria "Alimentação" no grupo "NECESSIDADES"

  @integracao
  Cenário: Outro usuário não consegue editar minha transação
    Dado que eu lancei 50.00 na categoria "Delivery"
    Quando outro usuário tenta editar essa transação para 1.00 na categoria "Delivery"
    Então a edição deve ser negada com a mensagem "Transação não encontrada."
    E ela deve estar com valor 50.00 na categoria "Delivery" no grupo "LAZER"

  @integracao
  Cenário: Filtrar transações por mês
    Dado que eu lancei 1500.00 na categoria "Salário" em "2026-09-05"
    E que eu lancei 55.90 na categoria "Delivery" em "2026-09-30"
    E que eu lancei 99.90 na categoria "Contas fixas" em "2026-10-01"
    Quando eu listo as transações de 9/2026
    Então devo receber 2 transações
    E todas devem ser de 9/2026

  @desempenho
  Cenário: Sistema deve responder rapidamente ao listar transações
    Dado que eu tenho 100 transações lançadas
    Quando eu solicito a lista de transações
    Então devo receber 100 transações
    E o tempo de resposta deve ser menor que 2 segundos
