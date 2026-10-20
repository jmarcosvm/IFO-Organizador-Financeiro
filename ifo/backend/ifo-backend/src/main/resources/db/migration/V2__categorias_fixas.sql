INSERT INTO categoria (nome, tipo, grupo) VALUES
  ('Salário',               'RECEITA', NULL),
  ('Freelance',             'RECEITA', NULL),
  ('Rendimentos',           'RECEITA', NULL),
  ('Mesada/Presente',       'RECEITA', NULL),
  ('Reembolso',             'RECEITA', NULL),
  ('Outras receitas',       'RECEITA', NULL),

  ('Moradia',               'DESPESA', 'NECESSIDADES'),
  ('Contas fixas',          'DESPESA', 'NECESSIDADES'),
  ('Alimentação',           'DESPESA', 'NECESSIDADES'),
  ('Transporte',            'DESPESA', 'NECESSIDADES'),
  ('Saúde',                 'DESPESA', 'NECESSIDADES'),
  ('Educação',              'DESPESA', 'NECESSIDADES'),
  ('Parcelas e dívidas',    'DESPESA', 'NECESSIDADES'),

  ('Delivery',              'DESPESA', 'LAZER'),
  ('Passeio',               'DESPESA', 'LAZER'),
  ('Assinaturas',           'DESPESA', 'LAZER'),
  ('Compras e vestuário',   'DESPESA', 'LAZER'),
  ('Viagem',                'DESPESA', 'LAZER'),
  ('Outros (lazer)',        'DESPESA', 'LAZER'),

  ('Renda fixa',            'DESPESA', 'INVESTIMENTOS'),
  ('Renda variável',        'DESPESA', 'INVESTIMENTOS'),
  ('Reserva de emergência', 'DESPESA', 'INVESTIMENTOS'),
  ('Previdência',           'DESPESA', 'INVESTIMENTOS');
