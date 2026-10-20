package com.fitzone.domain.model;

/**
 * Tipos de serviço adicional que podem ser vinculados a um agendamento.
 * Os nomes são iguais aos @DiscriminatorValue das subclasses de ServicoAdicional.
 */
public enum TipoServico {
    AVALIACAO_FISICA,
    NUTRICIONISTA,
    PERSONAL_TRAINER,
    LOCKER
}