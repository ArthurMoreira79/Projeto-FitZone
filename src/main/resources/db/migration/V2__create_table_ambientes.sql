-- Herança SINGLE_TABLE: SalaMusculacao, SalaYoga, SalaCrossfit e Piscina
-- dividem esta tabela; "tipo" é a coluna discriminadora.
-- O id NÃO é AUTO_INCREMENT: é calculado pela faixa de cada TipoAmbiente.
CREATE TABLE ambientes (
    id          INT          NOT NULL,
    tipo        VARCHAR(20)  NOT NULL,
    nome        VARCHAR(100) NOT NULL,
    valor_hora  DOUBLE       NOT NULL,
    observacoes VARCHAR(200) NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_ambientes_tipo
        CHECK (tipo IN ('MUSCULACAO', 'YOGA', 'CROSSFIT', 'PISCINA')),
    CONSTRAINT chk_ambientes_valor_hora CHECK (valor_hora > 0),
    -- cada tipo só aceita ids da sua faixa (espelha o enum TipoAmbiente)
    CONSTRAINT chk_ambientes_faixa CHECK (
           (tipo = 'MUSCULACAO' AND id BETWEEN 101 AND 120)
        OR (tipo = 'YOGA'       AND id BETWEEN 201 AND 220)
        OR (tipo = 'CROSSFIT'   AND id BETWEEN 301 AND 320)
        OR (tipo = 'PISCINA'    AND id BETWEEN 401 AND 420)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;