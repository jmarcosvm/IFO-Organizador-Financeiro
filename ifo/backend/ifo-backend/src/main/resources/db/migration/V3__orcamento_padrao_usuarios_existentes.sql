-- Quem se cadastrou antes de o cadastro criar o 50/30/20 fica sem orçamento; aqui damos o padrão a essas pessoas
INSERT INTO orcamento (id_usuario, grupo, percentual)
SELECT u.id_usuario, g.grupo, g.percentual
FROM usuario u
CROSS JOIN (VALUES ('NECESSIDADES', 50), ('LAZER', 30), ('INVESTIMENTOS', 20)) AS g(grupo, percentual)
WHERE NOT EXISTS (SELECT 1 FROM orcamento o WHERE o.id_usuario = u.id_usuario);
