#language: pt
Funcionalidade: Isolamento entre usuários
  Como dono dos meus dados financeiros
  Quero que nenhum outro usuário veja ou altere o que é meu
  Para que ninguém acesse dado alheio (RN11)

  # "Outro usuário" é sempre alguém recém-cadastrado, com o próprio token.
  # Dado de outro usuário responde 404 igual a inexistente: não revela que o id existe.

  Contexto:
    Dado que estou autenticado como um novo usuário

  @integracao @seguranca
  Cenário: Outro usuário não vê minhas transações, meu resumo nem meu CSV
    Dado que eu lancei 100,00 na categoria "Delivery"
    Quando outro usuário consulta "/api/transacoes"
    Então a lista deve estar vazia
    Quando outro usuário consulta "/api/resumo"
    Então o resumo dele deve estar zerado
    Quando outro usuário exporta o CSV do mês atual
    Então o CSV dele deve ter apenas o cabeçalho

  @integracao @seguranca
  Cenário: Outro usuário não vê minhas metas nem minhas recorrências
    Dado que eu criei a meta "Reserva" com objetivo de 1000,00
    E que eu criei uma despesa recorrente de 50,00 na categoria "Contas fixas"
    Quando outro usuário consulta "/api/metas"
    Então a lista deve estar vazia
    Quando outro usuário consulta "/api/recorrencias"
    Então a lista deve estar vazia

  @integracao @seguranca
  Cenário: Alterar meu orçamento não altera o orçamento dos outros
    Dado que eu mudei meu orçamento para 60/20/20
    Quando outro usuário consulta "/api/orcamento"
    Então o orçamento dele deve continuar 50/30/20

  @integracao @seguranca
  Cenário: Outro usuário não consegue editar minha recorrência
    Dado que eu criei uma despesa recorrente de 50,00 na categoria "Contas fixas"
    Quando outro usuário tenta editar essa recorrência para 1,00
    Então a resposta deve ter status 404
    E a mensagem de erro deve ser "Recorrência não encontrada."
    E a recorrência continua ativa para mim com valor 50,00

  @integracao @seguranca
  Cenário: Outro usuário não consegue pausar minha recorrência
    Dado que eu criei uma despesa recorrente de 50,00 na categoria "Contas fixas"
    Quando outro usuário tenta pausar essa recorrência
    Então a resposta deve ter status 404
    E a mensagem de erro deve ser "Recorrência não encontrada."
    E a recorrência continua ativa para mim com valor 50,00

  @integracao @seguranca
  Cenário: Outro usuário não consegue excluir minha recorrência
    Dado que eu criei uma despesa recorrente de 50,00 na categoria "Contas fixas"
    Quando outro usuário tenta excluir essa recorrência
    Então a resposta deve ter status 404
    E a mensagem de erro deve ser "Recorrência não encontrada."
    E a recorrência continua ativa para mim com valor 50,00

  @integracao @seguranca
  Cenário: Dados do usuário nunca expõem senha nem hash
    Quando eu consulto "/api/usuarios/me"
    Então a resposta deve ter status 200
    E a resposta não deve expor senha nem hash
