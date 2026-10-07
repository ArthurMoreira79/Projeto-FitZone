CREATE TABLE alunos (
    id              INT AUTO_INCREMENT NOT NULL,
    cpf             VARCHAR(11)  NOT NULL,
    nome            VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NOT NULL,
    telefone        VARCHAR(20)  NOT NULL,
    data_cadastro   DATE         NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_alunos_cpf UNIQUE (cpf),
    CONSTRAINT uk_alunos_email UNIQUE (email),
    CONSTRAINT chk_alunos_cpf CHECK (cpf REGEXP '^[0-9]{11}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;