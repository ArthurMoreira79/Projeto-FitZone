package com.fitzone.domain.service;

import com.fitzone.api.dto.RelatorioAmbienteDTO;
import com.fitzone.api.dto.RelatorioServicoDTO;
import com.fitzone.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RelatorioServiceTest {

    private static final LocalDate DATA = LocalDate.now().plusDays(7);

    @Autowired private RelatorioService relatorioService;
    @Autowired private AgendamentoService agendamentoService;
    @Autowired private AlunoService alunoService;
    @Autowired private AmbienteService ambienteService;

    @Test
    void ocupacaoTrazTipoHorasEValorDeCadaAmbienteInclusiveSemAgendamento() {
        Aluno aluno = alunoService.cadastrar(new Aluno("12345678901", "Ana", "ana@email.com", "11987654321", null));
        Ambiente sala = ambienteService.cadastrar(TipoAmbiente.MUSCULACAO, null); // R$ 100/h
        Ambiente yoga = ambienteService.cadastrar(TipoAmbiente.YOGA, null);
        Agendamento ag = agendamentoService.realizar(aluno.getId(), sala.getId(), DATA,
                LocalTime.parse("08:00"), LocalTime.parse("10:40")); // 2h -> R$ 200
        agendamentoService.adicionarServico(ag.getId(), new LockerAcademia(2)); // + R$ 10

        List<RelatorioAmbienteDTO> ocupacao = relatorioService.porAmbiente();

        RelatorioAmbienteDTO daSala = ocupacao.stream().filter(o -> o.ambienteId().equals(sala.getId())).findFirst().orElseThrow();
        assertThat(daSala.codigo()).isEqualTo("MUSCULACAO");
        assertThat(daSala.horasCobradas()).isEqualTo(2);
        assertThat(daSala.valorTotal()).isEqualByComparingTo("210");

        RelatorioAmbienteDTO daYoga = ocupacao.stream().filter(o -> o.ambienteId().equals(yoga.getId())).findFirst().orElseThrow();
        assertThat(daYoga.quantidadeAgendamentos()).isZero(); // ocupação zero também aparece
        assertThat(daYoga.valorTotal()).isEqualByComparingTo("0");
    }

    @Test
    void servicosVendidosUsamONomeLegivelDoTipo() {
        Aluno aluno = alunoService.cadastrar(new Aluno("12345678901", "Ana", "ana@email.com", "11987654321", null));
        Ambiente sala = ambienteService.cadastrar(TipoAmbiente.MUSCULACAO, null);
        Agendamento ag = agendamentoService.realizar(aluno.getId(), sala.getId(), DATA,
                LocalTime.parse("08:00"), LocalTime.parse("09:00"));
        agendamentoService.adicionarServico(ag.getId(), new AvaliacaoFisica());
        agendamentoService.adicionarServico(ag.getId(), new LockerAcademia(3));

        assertThat(relatorioService.porServico())
                .extracting(RelatorioServicoDTO::servico)
                .containsExactly("Avaliação Física", "Locker"); // ordem do enum, não "AvaliacaoFisica"
    }
}
