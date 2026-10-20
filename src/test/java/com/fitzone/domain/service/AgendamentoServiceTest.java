package com.fitzone.domain.service;

import com.fitzone.domain.exception.AmbienteIndisponivelException;
import com.fitzone.domain.exception.HorarioInvalidoException;
import com.fitzone.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AgendamentoServiceTest {

    private static final LocalDate DATA = LocalDate.now().plusDays(7);

    @Autowired private AgendamentoService agendamentoService;
    @Autowired private AlunoService alunoService;
    @Autowired private AmbienteService ambienteService;

    @Autowired private EntityManager entityManager;

    private Aluno aluno;
    private Ambiente sala; // Sala de Musculação: R$ 100/h

    // roda antes de CADA teste (dentro da mesma transação, então também sofre rollback)
    @BeforeEach
    void prepararCenario() {
        aluno = alunoService.cadastrar(new Aluno("12345678901", "Ana", "ana@email.com", "11987654321", null));
        sala = ambienteService.cadastrar(TipoAmbiente.MUSCULACAO, null);
    }

    private Agendamento agendar(String inicio, String fim) {
        return agendamentoService.realizar(aluno.getId(), sala.getId(), DATA,
                LocalTime.parse(inicio), LocalTime.parse(fim));
    }

    // ---------- regra de cobrança ----------

    @Test
    void deveCobrarSomenteHorasCompletas() {
        Agendamento ag = agendar("08:00", "10:40"); // 2h40 -> 2h

        assertThat(ag.getHorasCobradas()).isEqualTo(2);
        assertThat(ag.getValorTotal()).isEqualTo(200.0);
    }

    @Test
    void reservaMenorQueUmaHoraCobraUmaHoraCheia() {
        Agendamento ag = agendar("08:00", "08:40"); // 40min -> 1h

        assertThat(ag.getHorasCobradas()).isEqualTo(1);
        assertThat(ag.getValorTotal()).isEqualTo(100.0);
    }

    @Test
    void personalTrainerECobradoPorHoraDoAgendamento() {
        Agendamento ag = agendar("08:00", "10:40"); // 2h cobradas

        Agendamento comPersonal = agendamentoService.adicionarServico(ag.getId(), new PersonalTrainer());

        // 2h x R$100 (sala) + 2h x R$50 (personal)
        assertThat(comPersonal.getValorTotal()).isEqualTo(300.0);
    }

    @Test
    void lockerECobradoPorUnidade() {
        Agendamento ag = agendar("08:00", "09:00");

        Agendamento comLocker = agendamentoService.adicionarServico(ag.getId(), new LockerAcademia(3));

        // 1h x R$100 + 3 x R$5
        assertThat(comLocker.getValorTotal()).isEqualTo(115.0);
    }

    // ---------- conflito de horário ----------

    @Test
    void deveRecusarHorarioSobrepostoNoMesmoAmbiente() {
        agendar("08:00", "10:00");

        assertThatThrownBy(() -> agendar("09:00", "11:00"))
                .isInstanceOf(AmbienteIndisponivelException.class);
    }

    @Test
    void horarioEncostadoNaoConflita() {
        agendar("08:00", "09:00");

        Agendamento seguinte = agendar("09:00", "10:00");

        assertThat(seguinte.getId()).isNotNull();
    }

    @Test
    void deveRecusarHoraDeFimAntesDaHoraDeInicio() {
        assertThatThrownBy(() -> agendar("10:00", "09:00"))
                .isInstanceOf(HorarioInvalidoException.class);
    }

    // ---------- integridade ----------

    @Test
    void naoDeveExcluirAlunoComAgendamentos() {
        agendar("08:00", "09:00");

        // Simula uma requisição nova (como no Postman): grava no banco o que está pendente
        // e esvazia a memória do Hibernate, para o agendamento não estar mais "gerenciado".
        // Assim quem recusa a exclusão é o banco (ON DELETE RESTRICT), não o Hibernate.
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> alunoService.excluir(aluno.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}