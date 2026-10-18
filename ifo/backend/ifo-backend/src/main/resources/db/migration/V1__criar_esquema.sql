CREATE TABLE usuario (
    id_usuario    BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL UNIQUE,
    senha_hash    VARCHAR(100)  NOT NULL,
    renda_mensal  NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (renda_mensal >= 0),
    data_criacao  TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE TABLE categoria (
    id_categoria SMALLSERIAL PRIMARY KEY,
    nome         VARCHAR(50) NOT NULL UNIQUE,
    tipo         VARCHAR(10) NOT NULL CHECK (tipo IN ('RECEITA', 'DESPESA')),
    grupo        VARCHAR(15) CHECK (grupo IN ('NECESSIDADES', 'LAZER', 'INVESTIMENTOS')),
    CONSTRAINT ck_categoria_tipo_grupo CHECK (
        (tipo = 'RECEITA' AND grupo IS NULL) OR
        (tipo = 'DESPESA' AND grupo IS NOT NULL)
    )
);

CREATE TABLE transacao_recorrente (
    id_recorrencia     BIGSERIAL PRIMARY KEY,
    id_usuario         BIGINT        NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    id_categoria       SMALLINT      NOT NULL REFERENCES categoria(id_categoria) ON DELETE RESTRICT,
    valor              NUMERIC(12,2) NOT NULL CHECK (valor > 0),
    descricao          VARCHAR(255),
    dia_mes            SMALLINT      NOT NULL CHECK (dia_mes BETWEEN 1 AND 31),
    data_inicio        DATE          NOT NULL,
    data_fim           DATE,
    ativa              BOOLEAN       NOT NULL DEFAULT TRUE,
    ultima_competencia DATE,
    CHECK (data_fim IS NULL OR data_fim >= data_inicio)
);
CREATE INDEX idx_recorrente_usuario ON transacao_recorrente (id_usuario);

CREATE TABLE transacao (
    id_transacao   BIGSERIAL PRIMARY KEY,
    id_usuario     BIGINT        NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    id_categoria   SMALLINT      NOT NULL REFERENCES categoria(id_categoria) ON DELETE RESTRICT,
    id_recorrencia BIGINT        REFERENCES transacao_recorrente(id_recorrencia) ON DELETE SET NULL,
    valor          NUMERIC(12,2) NOT NULL CHECK (valor > 0),
    data         DATE          NOT NULL,
    descricao    VARCHAR(255),
    criado_em    TIMESTAMP     NOT NULL DEFAULT now()
);
CREATE INDEX idx_transacao_usuario_data ON transacao (id_usuario, data);

CREATE TABLE orcamento (
    id_orcamento BIGSERIAL PRIMARY KEY,
    id_usuario   BIGINT       NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    grupo        VARCHAR(15)  NOT NULL CHECK (grupo IN ('NECESSIDADES', 'LAZER', 'INVESTIMENTOS')),
    percentual   NUMERIC(5,2) NOT NULL CHECK (percentual >= 0 AND percentual <= 100),
    UNIQUE (id_usuario, grupo)
);

CREATE TABLE meta_financeira (
    id_meta        BIGSERIAL PRIMARY KEY,
    id_usuario     BIGINT        NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    titulo_meta    VARCHAR(100)  NOT NULL,
    valor_objetivo NUMERIC(12,2) NOT NULL CHECK (valor_objetivo > 0),
    valor_atual    NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (valor_atual >= 0),
    prazo          DATE
);
CREATE INDEX idx_meta_usuario ON meta_financeira (id_usuario);
