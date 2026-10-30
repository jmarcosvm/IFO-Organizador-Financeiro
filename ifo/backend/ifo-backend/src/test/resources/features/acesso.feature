#language: pt
Funcionalidade: Acesso às rotas protegidas
  Como dono dos dados financeiros
  Quero que só quem está autenticado consiga acessá-los
  Para que ninguém veja ou altere o que não é dele

  @integracao
  Esquema do Cenário: Rota protegida sem token é recusada
    Dado que não estou autenticado
    Quando eu consulto "<rota>"
    Então a resposta deve ter status 401
    E a mensagem de erro deve ser "Token ausente, inválido ou expirado."

    Exemplos:
      | rota                    |
      | /api/transacoes         |
      | /api/resumo             |
      | /api/orcamento          |
      | /api/metas              |
      | /api/recorrencias       |
      | /api/usuarios/me        |
      | /api/categorias         |

  @integracao
  Cenário: Token adulterado é recusado
    Dado que estou com um token adulterado
    Quando eu consulto "/api/transacoes"
    Então a resposta deve ter status 401
    E a mensagem de erro deve ser "Token ausente, inválido ou expirado."
