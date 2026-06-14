#language: pt
Funcionalidade: Autenticação de Usuários
  Como usuário do sistema IFO
  Quero fazer login na plataforma
  Para acessar minhas informações financeiras

  @integracao
  Cenário: Login com credenciais válidas
    Dado que o usuário "douglas@ifo.teste" existe no sistema
    Quando ele faz login com a senha "123456"
    Então o login deve ser realizado com sucesso
    E o sistema deve redirecionar para o dashboard

  @integracao
  Cenário: Login com credenciais inválidas
    Dado que o usuário está na tela de login
    Quando ele tenta login com email "errado@teste.com" e senha "errada"
    Então o sistema deve retornar erro de autenticação
    E a mensagem deve ser "Email ou senha inválidos"