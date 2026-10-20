#language: pt
Funcionalidade: Autenticação de Usuários
  Como usuário do sistema IFO
  Quero fazer login na plataforma
  Para acessar minhas informações financeiras

  @integracao
  Cenário: Login com credenciais válidas
    Dado que existe um usuário cadastrado com a senha "senhaSegura123"
    Quando ele faz login com a senha "senhaSegura123"
    Então o login deve ser realizado com sucesso
    E ele deve receber um token de acesso válido

  @integracao
  Cenário: Login com senha incorreta
    Dado que existe um usuário cadastrado com a senha "senhaSegura123"
    Quando ele faz login com a senha "senhaErrada"
    Então o sistema deve retornar erro de autenticação
    E a mensagem deve ser "E-mail ou senha inválidos."

  @integracao
  Cenário: Login com e-mail inexistente
    Quando alguém tenta login com email "naoexiste@ifo.teste" e senha "qualquer123"
    Então o sistema deve retornar erro de autenticação
    E a mensagem deve ser "E-mail ou senha inválidos."
