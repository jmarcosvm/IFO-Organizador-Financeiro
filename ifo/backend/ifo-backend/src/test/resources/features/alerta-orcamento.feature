#language: pt
Funcionalidade: Alerta de orçamento ao lançar despesas
  Como usuário do IFO
  Quero ser avisado quando um grupo do orçamento chegar perto do limite
  Para não estourar o orçamento sem perceber

  # Renda de 1500 no orçamento padrão 50/30/20: o limite de LAZER (30%) é 450,00
  # O alerta é só informativo: o lançamento é salvo em todos os casos (RN08)
  Contexto:
    Dado que estou autenticado como um novo usuário com renda de 1500,00

  @integracao
  Esquema do Cenário: Nível do alerta conforme o quanto do limite foi usado
    Quando eu lanço <valor> na categoria "Delivery"
    Então a resposta deve ter status 201
    E <situacao>

    Exemplos:
      | valor  | situacao                                                         |
      | 359,55 | não deve haver alerta de orçamento                               |
      | 360,00 | deve haver alerta de orçamento "ATENCAO" para o grupo "LAZER"    |
      | 450,00 | deve haver alerta de orçamento "ATENCAO" para o grupo "LAZER"    |
      | 450,05 | deve haver alerta de orçamento "ESTOURO" para o grupo "LAZER"    |

  @integracao
  Cenário: Receita nunca gera alerta de orçamento
    Quando eu lanço 5000,00 na categoria "Salário"
    Então a resposta deve ter status 201
    E não deve haver alerta de orçamento
