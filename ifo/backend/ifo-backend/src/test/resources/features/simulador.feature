#language: pt
Funcionalidade: Simulador de investimentos
  Como usuário do IFO
  Quero simular quanto meu dinheiro rende
  Para planejar meus investimentos sem salvar nada

  Contexto:
    Dado que estou autenticado como um novo usuário

  @integracao
  Cenário: Caso de referência com juros compostos
    Quando eu simulo aporte mensal de 300,00 a 1,0% ao mês por 12 meses com juros "COMPOSTO"
    Então o montante final deve ser 3804,75, o total investido 3600,00 e o rendimento 204,75
    E a evolução deve ter 12 meses

  @integracao
  Cenário: Juros simples com aplicação inicial
    Quando eu simulo aplicação inicial de 1000,00 com aporte mensal de 300,00 a 1,0% ao mês por 12 meses com juros "SIMPLES"
    Então o montante final deve ser 4918,00, o total investido 4600,00 e o rendimento 318,00

  @integracao
  Cenário: Taxa zero não gera rendimento
    Quando eu simulo aporte mensal de 300,00 a 0,0% ao mês por 12 meses com juros "COMPOSTO"
    Então o montante final deve ser 3600,00, o total investido 3600,00 e o rendimento 0,00

  @integracao
  Cenário: Prazo acima de 1200 meses é recusado
    Quando eu simulo aporte mensal de 300,00 a 1,0% ao mês por 1201 meses com juros "COMPOSTO"
    Então a resposta deve ter status 400
    E o erro deve apontar o campo "tempoMeses"
