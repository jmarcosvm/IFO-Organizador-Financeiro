#language: pt
Funcionalidade: Acesso às rotas protegidas
  Como dono dos dados financeiros
  Quero que só quem está autenticado consiga acessá-los
  Para que ninguém veja ou altere o que não é dele

  @integracao @seguranca
  Esquema do Cenário: Rota protegida sem token é recusada
    Dado que não estou autenticado
    Quando eu consulto "<rota>"
    Então a resposta deve ter status 401
    E a mensagem de erro deve ser "Token ausente, inválido ou expirado."

    Exemplos:
      | rota                       |
      | /api/transacoes            |
      | /api/transacoes/exportar   |
      | /api/resumo                |
      | /api/orcamento             |
      | /api/orcamento/status      |
      | /api/metas                 |
      | /api/recorrencias          |
      | /api/usuarios/me           |
      | /api/categorias            |

  @integracao @seguranca
  Cenário: Simulador também exige login
    Dado que não estou autenticado
    Quando eu simulo aporte mensal de 300,00 a 1,0% ao mês por 12 meses com juros "COMPOSTO"
    Então a resposta deve ter status 401

  @integracao @seguranca
  Cenário: Token adulterado é recusado
    Dado que estou com um token adulterado
    Quando eu consulto "/api/transacoes"
    Então a resposta deve ter status 401
    E a mensagem de erro deve ser "Token ausente, inválido ou expirado."

  @integracao @seguranca
  Cenário: Token expirado é recusado
    Dado que estou com um token expirado
    Quando eu consulto "/api/transacoes"
    Então a resposta deve ter status 401
    E a mensagem de erro deve ser "Token ausente, inválido ou expirado."

  @integracao @seguranca
  Cenário: Token sem assinatura é recusado
    Dado que estou com um token sem assinatura
    Quando eu consulto "/api/transacoes"
    Então a resposta deve ter status 401
    E a mensagem de erro deve ser "Token ausente, inválido ou expirado."

  @integracao @seguranca
  Cenário: Token assinado com outra chave é recusado
    Dado que estou com um token assinado com outra chave
    Quando eu consulto "/api/transacoes"
    Então a resposta deve ter status 401
    E a mensagem de erro deve ser "Token ausente, inválido ou expirado."
