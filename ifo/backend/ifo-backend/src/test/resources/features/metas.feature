#language: pt
Funcionalidade: Metas financeiras
  Como usuário do IFO
  Quero guardar dinheiro em metas
  Para acompanhar o quanto falta para chegar no objetivo

  Contexto:
    Dado que estou autenticado como um novo usuário

  @integracao
  Cenário: Aportes fazem a meta progredir até ser concluída
    Dado que eu criei a meta "Viagem" com objetivo de 1000,00
    Quando eu guardo 400,00 nessa meta
    Então a meta deve ter 400,00 guardados e 40,0% de progresso
    E a meta não deve estar concluída
    Quando eu guardo 600,00 nessa meta
    Então a meta deve ter 1000,00 guardados e 100,0% de progresso
    E a meta deve estar concluída

  @integracao
  Cenário: Aporte acima do objetivo é permitido e o progresso fica em 100%
    Dado que eu criei a meta "Notebook" com objetivo de 500,00
    E que eu guardei 500,00 nessa meta
    Quando eu guardo 100,00 nessa meta
    Então a meta deve ter 600,00 guardados e 100,0% de progresso
    E a meta deve estar concluída

  @integracao
  Cenário: Meta com objetivo zero é recusada
    Quando eu crio a meta "Inválida" com objetivo de 0,00
    Então a resposta deve ter status 400
    E o erro deve apontar o campo "valorObjetivo"

  @integracao
  Cenário: Outro usuário não consegue guardar dinheiro na minha meta
    Dado que eu criei a meta "Reserva" com objetivo de 1000,00
    Quando outro usuário tenta guardar 50,00 nessa meta
    Então a resposta deve ter status 404
    E a mensagem de erro deve ser "Meta não encontrada."
    E a meta continua existindo para mim com 0,00 guardados

  @integracao
  Cenário: Outro usuário não consegue excluir minha meta
    Dado que eu criei a meta "Reserva" com objetivo de 1000,00
    E que eu guardei 200,00 nessa meta
    Quando outro usuário tenta excluir essa meta
    Então a resposta deve ter status 404
    E a mensagem de erro deve ser "Meta não encontrada."
    E a meta continua existindo para mim com 200,00 guardados
