#language: pt
Funcionalidade: Gestão de Transações Financeiras
  Como usuário do IFO
  Quero gerenciar minhas transações
  Para controlar minhas finanças pessoais

  @unidade
  Cenário: Criar transação de receita com sucesso
    Dado que o sistema está pronto para receber transações
    Quando eu crio uma transação do tipo "RECEITA" com valor 1500.00
    Então a transação deve ser salva com sucesso
    E o valor deve ser positivo

  @unidade
  Cenário: Criar transação de despesa com sucesso
    Dado que o sistema está pronto para receber transações
    Quando eu crio uma transação do tipo "DESPESA" com valor 200.00
    Então a transação deve ser salva com sucesso
    E o valor deve ser negativo no cálculo do saldo

  @desempenho
  Cenário: Sistema deve responder rapidamente ao listar transações
    Dado que existem 100 transações no banco
    Quando eu solicito a lista de transações
    Então o tempo de resposta deve ser menor que 2 segundos