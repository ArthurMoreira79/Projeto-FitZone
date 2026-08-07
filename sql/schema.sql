-- =====================================================================
-- FitZone - Fase 2 - Script de criação das tabelas (MySQL)
-- =====================================================================
-- Pré-requisito: o banco "fitzone" já deve existir (CREATE DATABASE).
-- Rode este script inteiro dentro do schema "fitzone" antes de subir
-- a aplicação, já que a persistence.xml está configurada com
-- hibernate.hbm2ddl.auto=validate (o Hibernate NÃO cria tabelas sozinho).
--
-- Ordem de criação respeita as dependências de chave estrangeira:
-- alunos e ambientes primeiro, depois agendamentos, depois
-- servicos_adicionais (que depende de agendamentos).
-- =====================================================================

USE fitzone;

-- ---------------------------------------------------------------------
-- Tabela: alunos
-- Mapeia entidades.Aluno (classe concreta, sem herança)
-- ---------------------------------------------------------------------
CREATE TABLE alunos (
    cpf             VARCHAR(11)  NOT NULL,
    nome            VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NULL,
    telefone        VARCHAR(20)  NULL,
    data_cadastro   DATE         NOT NULL,
    PRIMARY KEY (cpf)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Tabela: ambientes
-- Mapeia a hierarquia entidades.Ambiente (SalaMusculacao, SalaYoga,
-- SalaCrossfit, Piscina) com estratégia SINGLE_TABLE + coluna "tipo"
-- como discriminador.
-- ---------------------------------------------------------------------
CREATE TABLE ambientes (
    id          VARCHAR(10)  NOT NULL,
    tipo        VARCHAR(20)  NOT NULL,   -- discriminador: MUSCULACAO | YOGA | CROSSFIT | PISCINA
    nome        VARCHAR(100) NOT NULL,
    valor_hora  DOUBLE       NOT NULL,
    observacoes VARCHAR(200) NULL,       -- texto livre e opcional digitado pelo usuário
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Tabela: agendamentos
-- Mapeia entidades.Agendamento. ID agora é gerado pelo próprio banco
-- (AUTO_INCREMENT), substituindo o antigo contador manual em memória.
-- ---------------------------------------------------------------------
CREATE TABLE agendamentos (
    id                  INT AUTO_INCREMENT NOT NULL,
    aluno_cpf           VARCHAR(11)  NOT NULL,
    ambiente_id         VARCHAR(10)  NOT NULL,
    data_agendamento    DATE         NOT NULL,
    hora_inicio         TIME         NOT NULL,
    hora_fim            TIME         NOT NULL,
    valor_total         DOUBLE       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_agendamento_aluno
        FOREIGN KEY (aluno_cpf) REFERENCES alunos(cpf),
    CONSTRAINT fk_agendamento_ambiente
        FOREIGN KEY (ambiente_id) REFERENCES ambientes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Tabela: servicos_adicionais
-- Mapeia a hierarquia entidades.ServicoAdicional (AvaliacaoFisica,
-- Nutricionista, PersonalTrainer, LockerAcademia) com SINGLE_TABLE.
-- A coluna "quantidade" só é usada por LockerAcademia; fica NULL
-- para os demais tipos.
-- ---------------------------------------------------------------------
CREATE TABLE servicos_adicionais (
    id              BIGINT AUTO_INCREMENT NOT NULL,
    tipo            VARCHAR(30) NOT NULL,  -- discriminador: AVALIACAO_FISICA | NUTRICIONISTA | PERSONAL_TRAINER | LOCKER
    quantidade      INT NULL,
    agendamento_id  INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_servico_agendamento
        FOREIGN KEY (agendamento_id) REFERENCES agendamentos(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- MIGRAÇÃO (rodar só se o banco já existia antes desta mudança)
-- =====================================================================
-- Se você criou as tabelas ANTES da coluna "observacoes" existir neste
-- script, rode só esta linha (não precisa recriar nada):
--
-- ALTER TABLE ambientes ADD COLUMN observacoes VARCHAR(200) NULL;