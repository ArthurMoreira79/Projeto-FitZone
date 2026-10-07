-- Herança SINGLE_TABLE: AvaliacaoFisica, Nutricionista, PersonalTrainer e
-- LockerAcademia. "quantidade" só é usada pelo LOCKER (NULL nos demais).
CREATE TABLE servicos_adicionais (
    id              BIGINT AUTO_INCREMENT NOT NULL,
    tipo            VARCHAR(30) NOT NULL,  -- AVALIACAO_FISICA | NUTRICIONISTA | PERSONAL_TRAINER | LOCKER
    quantidade      INT NULL,
    agendamento_id  INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_servico_agendamento
        FOREIGN KEY (agendamento_id) REFERENCES agendamentos(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_servicos_tipo
        CHECK (tipo IN ('AVALIACAO_FISICA', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'LOCKER')),
    CONSTRAINT chk_servicos_quantidade CHECK (
        (tipo = 'LOCKER' AND quantidade > 0)
        OR (tipo <> 'LOCKER' AND quantidade IS NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;