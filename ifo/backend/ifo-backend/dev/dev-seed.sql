-- Dados de demonstração do IFO (SÓ para desenvolvimento e apresentação).
-- NÃO é uma migration do Flyway: fica fora de src/main/resources de propósito, para nunca rodar sozinho.
-- Pré-requisito: o backend já subiu pelo menos uma vez (o Flyway criou as tabelas e as categorias).
--
-- Como aplicar (ver README, "Dados de demonstração"):
--   docker cp ifo/backend/ifo-backend/dev/dev-seed.sql ifo-db:/tmp/dev-seed.sql
--   docker exec ifo-db sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 -f /tmp/dev-seed.sql'
--
-- Pode rodar quantas vezes quiser: apaga e recria só os dois usuários de demonstração
-- (o ON DELETE CASCADE leva junto as transações, metas, recorrências e o orçamento deles).
-- Login: demo@ifo.teste / demo12345   (e outro@ifo.teste / demo12345, para mostrar o isolamento entre usuários)
--
-- As datas são relativas a hoje, então a demonstração sempre aparece no mês atual e nos dois anteriores.
-- Mês atual do demo (renda 5000 -> limites 2500 / 1500 / 1000):
--   NECESSIDADES 86,8% = ATENCAO · LAZER 110% = ESTOURO · INVESTIMENTOS 60% = OK

BEGIN;

-- Só para gerar o hash BCrypt da senha dentro do próprio banco (o BCrypt do pgcrypto, "$2a$", é
-- aceito pelo Spring Security). Removida no fim: o esquema fica exatamente como o Flyway deixou.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

DELETE FROM usuario WHERE email IN ('demo@ifo.teste', 'outro@ifo.teste');

INSERT INTO usuario (nome, email, senha_hash, renda_mensal) VALUES
  ('Usuário Demo',  'demo@ifo.teste',  crypt('demo12345', gen_salt('bf', 10)), 5000.00),
  ('Outro Usuário', 'outro@ifo.teste', crypt('demo12345', gen_salt('bf', 10)), 3000.00);

-- Orçamento padrão 50/30/20 (o cadastro pela API faz o mesmo)
INSERT INTO orcamento (id_usuario, grupo, percentual)
SELECT u.id_usuario, g.grupo, g.percentual
FROM usuario u
CROSS JOIN (VALUES ('NECESSIDADES', 50), ('LAZER', 30), ('INVESTIMENTOS', 20)) AS g(grupo, percentual)
WHERE u.email IN ('demo@ifo.teste', 'outro@ifo.teste');

-- Transações: mes = 0 (atual), -1 (anterior), -2; dia = dia do mês (até 28, existe em todo mês)
INSERT INTO transacao (id_usuario, id_categoria, valor, data, descricao)
SELECT u.id_usuario, c.id_categoria, t.valor,
       (date_trunc('month', current_date)::date + t.mes * INTERVAL '1 month')::date + (t.dia - 1),
       t.descricao
FROM (VALUES
  -- demo, mês atual
  ('demo@ifo.teste',  0,  1, 'Salário',               5000.00, 'Salário do mês'),
  ('demo@ifo.teste',  0,  2, 'Moradia',               1400.00, 'Aluguel'),
  ('demo@ifo.teste',  0,  8, 'Contas fixas',           250.00, 'Conta de luz'),
  ('demo@ifo.teste',  0,  6, 'Alimentação',            520.00, 'Mercado do mês'),
  ('demo@ifo.teste',  0,  9, 'Delivery',               420.00, 'Pizza e hambúrguer'),
  ('demo@ifo.teste',  0, 12, 'Passeio',                380.00, 'Cinema e jantar'),
  ('demo@ifo.teste',  0, 15, 'Assinaturas',            150.00, 'Streaming e música'),
  ('demo@ifo.teste',  0, 18, 'Compras e vestuário',    700.00, 'Tênis novo'),
  ('demo@ifo.teste',  0,  5, 'Renda fixa',             600.00, 'Aporte no CDB'),
  -- demo, mês anterior
  ('demo@ifo.teste', -1,  1, 'Salário',               5000.00, 'Salário do mês'),
  ('demo@ifo.teste', -1, 20, 'Freelance',              800.00, 'Projeto de site'),
  ('demo@ifo.teste', -1,  2, 'Moradia',               1400.00, 'Aluguel'),
  ('demo@ifo.teste', -1,  8, 'Contas fixas',           240.00, 'Conta de luz'),
  ('demo@ifo.teste', -1,  6, 'Alimentação',            560.00, 'Mercado do mês'),
  ('demo@ifo.teste', -1, 11, 'Transporte',             180.00, 'Passagens'),
  ('demo@ifo.teste', -1,  9, 'Delivery',               300.00, 'Delivery do mês'),
  ('demo@ifo.teste', -1, 15, 'Assinaturas',            150.00, 'Streaming e música'),
  ('demo@ifo.teste', -1, 22, 'Viagem',                 400.00, 'Fim de semana na praia'),
  ('demo@ifo.teste', -1,  5, 'Renda fixa',            1000.00, 'Aporte no CDB'),
  -- demo, dois meses atrás
  ('demo@ifo.teste', -2,  1, 'Salário',               5000.00, 'Salário do mês'),
  ('demo@ifo.teste', -2,  2, 'Moradia',               1400.00, 'Aluguel'),
  ('demo@ifo.teste', -2,  8, 'Contas fixas',           230.00, 'Conta de luz'),
  ('demo@ifo.teste', -2,  6, 'Alimentação',            450.00, 'Mercado do mês'),
  ('demo@ifo.teste', -2, 13, 'Saúde',                  100.00, 'Farmácia'),
  ('demo@ifo.teste', -2,  9, 'Delivery',               280.00, 'Delivery do mês'),
  ('demo@ifo.teste', -2, 16, 'Passeio',                200.00, 'Parque e lanche'),
  ('demo@ifo.teste', -2,  5, 'Renda fixa',             500.00, 'Aporte no CDB'),
  ('demo@ifo.teste', -2, 25, 'Reserva de emergência',  300.00, 'Reserva do mês'),
  -- outro usuário (para mostrar que um não vê os dados do outro)
  ('outro@ifo.teste', 0,  1, 'Salário',               3000.00, 'Salário do mês'),
  ('outro@ifo.teste', 0,  3, 'Alimentação',            400.00, 'Mercado'),
  ('outro@ifo.teste', 0,  8, 'Delivery',                90.00, 'Lanche')
) AS t(email, mes, dia, categoria, valor, descricao)
JOIN usuario u   ON u.email = t.email
JOIN categoria c ON c.nome  = t.categoria;

-- Despesas recorrentes do demo:
--  · Internet: ativa, dia 10, já gerada nos dois meses passados. O mês atual é gerado pelo job das 00:05
--    ou na hora em que a recorrência for editada pela tela (RN15).
--  · Academia: pausada, não gera nada (RN17).
INSERT INTO transacao_recorrente
  (id_usuario, id_categoria, valor, descricao, dia_mes, data_inicio, data_fim, ativa, ultima_competencia)
SELECT u.id_usuario, c.id_categoria, r.valor, r.descricao, r.dia_mes,
       (date_trunc('month', current_date)::date + r.mes_inicio * INTERVAL '1 month')::date + (r.dia_mes - 1),
       NULL::date, r.ativa,
       CASE WHEN r.ultima_mes IS NULL THEN NULL
            ELSE (date_trunc('month', current_date)::date + r.ultima_mes * INTERVAL '1 month')::date END
FROM (VALUES
  ('demo@ifo.teste', 'Contas fixas', 99.90, 'Internet', 10::smallint, -2, -1,   TRUE),
  ('demo@ifo.teste', 'Assinaturas',  89.90, 'Academia', 14::smallint, -1, NULL, FALSE)
) AS r(email, categoria, valor, descricao, dia_mes, mes_inicio, ultima_mes, ativa)
JOIN usuario u   ON u.email = r.email
JOIN categoria c ON c.nome  = r.categoria;

-- As transações já geradas da Internet (meses -2 e -1), ligadas à recorrência como o gerador faria
INSERT INTO transacao (id_usuario, id_categoria, id_recorrencia, valor, data, descricao)
SELECT r.id_usuario, r.id_categoria, r.id_recorrencia, r.valor,
       (date_trunc('month', current_date)::date + m.mes * INTERVAL '1 month')::date + (r.dia_mes - 1),
       r.descricao
FROM transacao_recorrente r
JOIN usuario u ON u.id_usuario = r.id_usuario AND u.email = 'demo@ifo.teste'
CROSS JOIN (VALUES (-2), (-1)) AS m(mes)
WHERE r.descricao = 'Internet';

-- Metas do demo: em andamento, concluída (RN09) e recém-começada
INSERT INTO meta_financeira (id_usuario, titulo_meta, valor_objetivo, valor_atual, prazo)
SELECT u.id_usuario, m.titulo, m.objetivo, m.atual, current_date + m.dias
FROM (VALUES
  ('Reserva de emergência', 6000.00, 2400.00, 365),
  ('Viagem de férias',      3500.00, 3500.00, 120),
  ('Notebook novo',         4500.00,  450.00, 240)
) AS m(titulo, objetivo, atual, dias)
CROSS JOIN usuario u
WHERE u.email = 'demo@ifo.teste';

DROP EXTENSION IF EXISTS pgcrypto;

COMMIT;
