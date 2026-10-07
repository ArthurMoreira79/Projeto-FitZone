CREATE TABLE agendamentos (
    id                  INT AUTO_INCREMENT NOT NULL,
    aluno_id            INT          NOT NULL,
    ambiente_id         INT          NOT NULL,
    data_agendamento    DATE         NOT NULL,
    hora_inicio         TIME         NOT NULL,
    hora_fim            TIME         NOT NULL,
    valor_total         DOUBLE       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_agendamento_aluno
        FOREIGN KEY (aluno_id) REFERENCES alunos(id) ON DELETE RESTRICT,
    CONSTRAINT fk_agendamento_ambiente
        FOREIGN KEY (ambiente_id) REFERENCES ambientes(id) ON DELETE RESTRICT,
    CONSTRAINT chk_agendamentos_horario CHECK (hora_fim > hora_inicio),
    CONSTRAINT chk_agendamentos_valor CHECK (valor_total > 0),
    INDEX idx_agendamentos_conflito (ambiente_id, data_agendamento, hora_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;