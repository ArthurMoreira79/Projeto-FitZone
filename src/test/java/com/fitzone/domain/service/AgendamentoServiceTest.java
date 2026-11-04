package com.fitzone.domain.service;

import java.math.BigDecimal;

import com.fitzone.domain.exception.AmbienteIndisponivelException;
import com.fitzone.domain.exception.HorarioInvalidoException;
import com.fitzone.domain.exception.RegistroEmUsoException;
import com.fitzone.domain.exception.ServicoNaoEncontradoException;
import com.fitzone.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
        assertThat(ag.getValorTotal()).isEqualByComparingTo("200");
    }

    @Test
    void reservaMenorQueUmaHoraCobraUmaHoraCheia() {
        Agendamento ag = agendar("08:00", "08:40"); // 40min -> 1h

        assertThat(ag.getHorasCobradas()).isEqualTo(1);
        assertThat(ag.getValorTotal()).isEqualByComparingTo("100");
    }

    @Test
    void personalTrainerECobradoPorHoraDoAgendamento() {
        Agendamento ag = agendar("08:00", "10:40"); // 2h cobradas

        Agendamento comPersonal = agendamentoService.adicionarServico(ag.getId(), new PersonalTrainer());

        // 2h x R$100 (sala) + 2h x R$50 (personal)
        assertThat(comPersonal.getValorTotal()).isEqualByComparingTo("300");
    }

    @Test
    void lockerECobradoPorUnidade() {
        Agendamento ag = agendar("08:00", "09:00");

        Agendamento comLocker = agendamentoService.adicionarServico(ag.getId(), new LockerAcademia(3));

        // 1h x R$100 + 3 x R$5
        assertThat(comLocker.getValorTotal()).isEqualByComparingTo("115");
    }

    @Test
    void cadaServicoCobraOPrecoDoSeuTipo() {
        Agendamento ag = agendar("08:00", "10:00"); // 2h cobradas
        ServicoAdicional[] servicos = {
                new AvaliacaoFisica(), new Nutricionista(), new PersonalTrainer(), new LockerAcademia(3)
        };

        for (ServicoAdicional servico : servicos) {
            agendamentoService.adicionarServico(ag.getId(), servico);
            TipoServico tipo = servico.getTipoServico();

            // cada subclasse tem que estar ligada ao seu próprio enum (pega copia-e-cola errado)
            assertThat(tipo.name()).isEqualTo(servico.getClass().getAnnotation(jakarta.persistence.DiscriminatorValue.class).value());
            BigDecimal esperado = switch (tipo.getCobranca()) {
                case FIXO -> tipo.getValor();
                case POR_HORA -> tipo.getValor().multiply(BigDecimal.valueOf(2));
                case POR_UNIDADE -> tipo.getValor().multiply(BigDecimal.valueOf(3));
            };
            assertThat(servico.getValorTotal()).isEqualByComparingTo(esperado);
        }

        assertThat(servicos[3].getDescricao()).isEqualTo("Locker (3 unidades)");
        assertThat(new LockerAcademia(1).getDescricao()).isEqualTo("Locker (1 unidade)");
    }

    @Test
    void removerServicoRecalculaOTotal() {
        Agendamento ag = agendar("08:00", "10:00");                                   // 2h x R$100 = 200
        agendamentoService.adicionarServico(ag.getId(), new PersonalTrainer());        // + 2h x R$50
        agendamentoService.adicionarServico(ag.getId(), new LockerAcademia(2));        // + 2 x R$5
        entityManager.flush(); // o INSERT dos serviços gera os ids (IDENTITY)

        Long idPersonal = ag.getServicosAdicionais().stream()
                .filter(s -> s instanceof PersonalTrainer).findFirst().orElseThrow().getId();

        Agendamento atualizado = agendamentoService.removerServico(ag.getId(), idPersonal);

        assertThat(atualizado.getValorTotal()).isEqualByComparingTo("210");
        assertThat(atualizado.getServicosAdicionais())
                .extracting(ServicoAdicional::getTipoServico)
                .containsExactly(TipoServico.LOCKER);
    }

    @Test
    void naoRemoveServicoDeOutroAgendamento() {
        Agendamento comServico = agendar("08:00", "09:00");
        Agendamento outro = agendar("09:00", "10:00");
        agendamentoService.adicionarServico(comServico.getId(), new Nutricionista());
        entityManager.flush();
        Long idServico = comServico.getServicosAdicionais().get(0).getId();

        assertThatThrownBy(() -> agendamentoService.removerServico(outro.getId(), idServico))
                .isInstanceOf(ServicoNaoEncontradoException.class);
        assertThat(comServico.getServicosAdicionais()).hasSize(1); // continua lá
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

    // ---------- listagem com filtros ----------

    @Test
    void deveFiltrarPorDataEAmbienteEmOrdemDeHorario() {
        Ambiente yoga = ambienteService.cadastrar(TipoAmbiente.YOGA, null);
        Agendamento dasDez = agendar("10:00", "11:00");
        Agendamento dasOito = agendar("08:00", "09:00");
        agendamentoService.realizar(aluno.getId(), yoga.getId(), DATA, LocalTime.parse("08:00"), LocalTime.parse("09:00"));
        agendamentoService.realizar(aluno.getId(), sala.getId(), DATA.plusDays(1), LocalTime.parse("08:00"), LocalTime.parse("09:00"));

        assertThat(agendamentoService.listar(DATA, sala.getId(), null))
                .extracting(Agendamento::getId)
                .containsExactly(dasOito.getId(), dasDez.getId()); // ordem de horário, não de cadastro
        assertThat(agendamentoService.listar(DATA, null, null)).hasSize(3);
        assertThat(agendamentoService.listar(null, null, aluno.getId())).hasSize(4);
    }

    // ---------- orçamento ----------

    @Test
    void orcamentoUsaAMesmaRegraDeCobrancaSemGravarNada() {
        Agendamento previa = agendamentoService.orcar(sala.getId(), LocalTime.parse("08:00"), LocalTime.parse("10:40"));

        assertThat(previa.getHorasCobradas()).isEqualTo(2);  // 2h40 -> 2h
        assertThat(previa.getValorTotal()).isEqualByComparingTo("200");
        assertThat(previa.getId()).isNull();                  // nunca passou pelo save
        assertThat(agendamentoService.listar(null, sala.getId(), null)).isEmpty();
    }

    @Test
    void orcamentoRecusaHoraDeFimAntesDaHoraDeInicio() {
        assertThatThrownBy(() -> agendamentoService.orcar(sala.getId(), LocalTime.parse("10:00"), LocalTime.parse("09:00")))
                .isInstanceOf(HorarioInvalidoException.class);
    }

    // ---------- integridade ----------

    @Test
    void naoDeveExcluirAlunoComAgendamentos() {
        agendar("08:00", "09:00");

        // Simula uma requisição nova (como no Postman): grava no banco o que está pendente
        // e esvazia a memória do Hibernate, para o agendamento não estar mais "gerenciado".
        entityManager.flush();
        entityManager.clear();

        // o service conta os agendamentos antes e recusa com mensagem clara
        // (o ON DELETE RESTRICT do banco fica como última linha de defesa)
        assertThatThrownBy(() -> alunoService.excluir(aluno.getId()))
                .isInstanceOf(RegistroEmUsoException.class)
                .hasMessage("O aluno Ana tem 1 agendamento e não pode ser excluído. Cancele os agendamentos antes.");
    }

    @Test
    void naoDeveExcluirAmbienteComAgendamentos() {
        agendar("08:00", "09:00");
        agendar("09:00", "10:00");
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> ambienteService.excluir(sala.getId()))
                .isInstanceOf(RegistroEmUsoException.class)
                .hasMessageContaining("Sala de Musculação 101 tem 2 agendamentos");
    }
}