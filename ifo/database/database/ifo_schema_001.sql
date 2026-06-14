-- =====================================================
-- IFO - Organizador Financeiro Inteligente
-- Script de criação e teste do Banco de Dados
-- PostgreSQL / Docker
-- =====================================================


-- =====================================================
-- ENUMS (Tipos)
-- =====================================================

CREATE TYPE tipo_transacao AS ENUM ('RECEITA', 'DESPESA');
CREATE TYPE tipo_conta AS ENUM ('CORRENTE', 'POUPANCA', 'CARTEIRA');
CREATE TYPE tipo_juros AS ENUM ('COMPOSTO', 'SIMPLES');

-- =====================================================
-- TABELAS
-- =====================================================

-- USUARIO
CREATE TABLE usuario (
    id_usuario SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    renda_mensal DECIMAL(10,2) DEFAULT 0.00,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CONTA/CARTEIRA
CREATE TABLE conta_carteira (
    id_conta SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL,
    nome_conta VARCHAR(50) NOT NULL,
    tipo_conta tipo_conta NOT NULL,
    saldo_inicial DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
);

-- CATEGORIA
CREATE TABLE categoria (
    id_categoria SERIAL PRIMARY KEY,
    id_usuario INTEGER,
    nome_categoria VARCHAR(50) NOT NULL,
    tipo tipo_transacao NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE SET NULL
);

-- TRANSACAO (Tabela única para Receita e Despesa)
CREATE TABLE transacao (
    id_transacao SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL,
    id_conta INTEGER NOT NULL,
    id_categoria INTEGER NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    tipo tipo_transacao NOT NULL,
    data DATE NOT NULL,
    descricao VARCHAR(255),
    data_vencimento DATE,
    paga BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_conta) REFERENCES conta_carteira(id_conta) ON DELETE RESTRICT,
    FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria) ON DELETE RESTRICT
);


-- ORÇAMENTO MENSAL
CREATE TABLE orcamento_mensal (
    id_orcamento SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL,
    id_categoria INTEGER NOT NULL,
    limite_valor DECIMAL(10,2) NOT NULL,
    mes_referencia DATE NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria) ON DELETE RESTRICT
);

-- META FINANCEIRA
CREATE TABLE meta_financeira (
    id_meta SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL,
    titulo_meta VARCHAR(100) NOT NULL,
    valor_objetivo DECIMAL(10,2) NOT NULL,
    valor_atual DECIMAL(10,2) DEFAULT 0.00,
    prazo DATE,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
);

-- INVESTIMENTO
CREATE TABLE investimento (
    id_investimento SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL,
    valor_mensal DECIMAL(10,2) NOT NULL,
    taxa_juros DECIMAL(5,2) NOT NULL,
    tempo_meses INTEGER NOT NULL,
    tipo_juros tipo_juros DEFAULT 'COMPOSTO',
    montante_final DECIMAL(10,2),
    data_simulacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
);

-- =====================================================
-- ÍNDICES PARA PERFORMANCE
-- =====================================================

CREATE INDEX idx_transacao_usuario ON transacao(id_usuario);
CREATE INDEX idx_transacao_data ON transacao(data);
CREATE INDEX idx_transacao_tipo ON transacao(tipo);
CREATE INDEX idx_orcamento_usuario ON orcamento_mensal(id_usuario);
CREATE INDEX idx_orcamento_mes ON orcamento_mensal(mes_referencia);
CREATE INDEX idx_meta_usuario ON meta_financeira(id_usuario);
CREATE INDEX idx_investimento_usuario ON investimento(id_usuario);

-- =====================================================
-- DADOS DE TESTE (STORYTELLING DOUGLAS)
-- =====================================================

-- USUÁRIO: DOUGLAS
INSERT INTO usuario (nome, email, senha, renda_mensal) VALUES
('Douglas Teste', 'douglas@ifo.teste', '$2y$10$92IXUNpkjO0rOQ5byMi', 1500.00);

-- CONTA: CARTEIRA PRINCIPAL
INSERT INTO conta_carteira (id_usuario, nome_conta, tipo_conta, saldo_inicial) VALUES
(1, 'Carteira Principal', 'CARTEIRA', 0.00);

-- CATEGORIAS: RECEITAS
INSERT INTO categoria (id_usuario, nome_categoria, tipo) VALUES
(1, 'Mesada', 'RECEITA'),
(1, 'Freelance', 'RECEITA');

-- CATEGORIAS: DESPESAS
INSERT INTO categoria (id_usuario, nome_categoria, tipo) VALUES
(1, 'Lazer', 'DESPESA'),
(1, 'Alimentação', 'DESPESA'),
(1, 'Transporte', 'DESPESA'),
(1, 'Investimento', 'DESPESA');

-- TRANSAÇÕES: RECEITAS
INSERT INTO transacao (id_usuario, id_conta, id_categoria, valor, tipo, data, descricao, paga) VALUES
(1, 1, 1, 1500.00, 'RECEITA', '2025-04-01', 'Mesada dos pais', TRUE),
(1, 1, 2, 500.00, 'RECEITA', '2025-04-10', 'Freelance de fim de semana', TRUE);

-- TRANSAÇÕES: DESPESAS FIXAS (NECESSIDADES - 50%)
INSERT INTO transacao (id_usuario, id_conta, id_categoria, valor, tipo, data, descricao, data_vencimento, paga) VALUES
(1, 1, 3, 600.00, 'DESPESA', '2025-04-05', 'Ajuda em casa', '2025-04-05', TRUE),
(1, 1, 4, 300.00, 'DESPESA', '2025-04-08', 'Supermercado', '2025-04-08', TRUE),
(1, 1, 5, 150.00, 'DESPESA', '2025-04-12', 'Ônibus/Uber', '2025-04-12', TRUE);

-- TRANSAÇÕES: DESPESAS VARIÁVEIS (LAZER - 30%)
INSERT INTO transacao (id_usuario, id_conta, id_categoria, valor, tipo, data, descricao, data_vencimento, paga) VALUES
(1, 1, 3, 200.00, 'DESPESA', '2025-04-15', 'Cinema com amigos', '2025-04-15', TRUE),
(1, 1, 4, 100.00, 'DESPESA', '2025-04-18', 'Lanche na faculdade', '2025-04-18', TRUE);

-- TRANSAÇÃO: INVESTIMENTO (20% - REGRA 50/30/20)
INSERT INTO transacao (id_usuario, id_conta, id_categoria, valor, tipo, data, descricao, paga) VALUES
(1, 1, 6, 300.00, 'DESPESA', '2025-04-20', 'Guardar para console', TRUE);

-- META: COMPRAR CONSOLE
INSERT INTO meta_financeira (id_usuario, titulo_meta, valor_objetivo, valor_atual, prazo) VALUES
(1, 'Comprar Console Novo', 3000.00, 300.00, '2025-12-31');

-- ORÇAMENTOS MENSAL (Regra 50/30/20)
INSERT INTO orcamento_mensal (id_usuario, id_categoria, limite_valor, mes_referencia) VALUES
(1, 4, 750.00, '2025-04-01'),   -- Alimentação: 50% da renda
(1, 3, 450.00, '2025-04-01'),   -- Lazer: 30% da renda
(1, 6, 300.00, '2025-04-01');   -- Investimento: 20% da renda

-- INVESTIMENTO: SIMULAÇÃO
INSERT INTO investimento (id_usuario, valor_mensal, taxa_juros, tempo_meses, tipo_juros, montante_final) VALUES
(1, 300.00, 1.00, 12, 'COMPOSTO', 3804.75);

-- =====================================================
-- CONSULTAS DE VALIDAÇÃO (TESTES)
-- =====================================================

-- Ver saldo total
SELECT SUM(CASE WHEN tipo = 'RECEITA' THEN valor ELSE -valor END) as saldo
FROM transacao WHERE id_usuario = 1;

-- Ver gastos por categoria
SELECT c.nome_categoria, SUM(t.valor) as total_gasto
FROM transacao t JOIN categoria c ON t.id_categoria = c.id_categoria
WHERE t.id_usuario = 1 AND t.tipo = 'DESPESA'
GROUP BY c.nome_categoria;

-- Ver progresso da meta
SELECT 
  titulo_meta,
  'R$ ' || valor_objetivo as objetivo_formatado,
  ROUND((valor_atual / valor_objetivo * 100), 2)::TEXT || '%' as percentual
FROM meta_financeira;

UPDATE usuario 
SET senha = '123456' 
WHERE email = 'douglas@ifo.teste';

-- Criar nova transação
INSERT INTO transacao (id_usuario, id_conta, id_categoria, valor, tipo, data, descricao, paga)
VALUES (1, 1, 3, 250.00, 'DESPESA', '2025-05-20', 'Teste de criação', TRUE);

DELETE FROM transacao 
WHERE descricao = 'Teste de criação' 
AND id_usuario = 1;

-- Alterar tipo da coluna para interface
ALTER TABLE transacao ALTER COLUMN tipo TYPE VARCHAR(20);
ALTER TABLE categoria ALTER COLUMN tipo TYPE VARCHAR(20);
ALTER TABLE investimento ALTER COLUMN tipo_juros TYPE VARCHAR(20);
ALTER TABLE conta_carteira ALTER COLUMN tipo_conta TYPE VARCHAR(20);


-- Teste de consulta de transação
SELECT 
    id_transacao,
    descricao,
    valor,
    tipo,
    data,
    paga
FROM transacao
WHERE id_usuario = 1
ORDER BY data DESC;

SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'categoria' 
AND table_schema = 'public'
ORDER BY ordinal_position;

-- Tabela transacao
ALTER TABLE transacao 
ADD COLUMN IF NOT EXISTS categoria_tipo VARCHAR(255);

-- Tabela orcamento_mensal
ALTER TABLE orcamento_mensal 
ADD COLUMN IF NOT EXISTS categoria VARCHAR(255),
ADD COLUMN IF NOT EXISTS ano INTEGER;

ALTER TABLE orcamento_mensal ALTER COLUMN categoria DROP NOT NULL;

SELECT * FROM orcamento_mensal WHERE id_usuario = 4;
SELECT * FROM meta_financeira WHERE id_usuario = 4;
SELECT * FROM transacao WHERE id_usuario = 4;

SELECT * FROM usuario;
