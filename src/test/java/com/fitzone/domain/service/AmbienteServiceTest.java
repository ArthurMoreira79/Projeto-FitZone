package com.fitzone.domain.service;

import com.fitzone.domain.exception.LimiteAmbienteExcedidoException;
import com.fitzone.domain.model.Ambiente;
import com.fitzone.domain.model.TipoAmbiente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AmbienteServiceTest {

    @Autowired
    private AmbienteService ambienteService;

    @Test
    void primeiroAmbienteDoTipoRecebeOInicioDaFaixa() {
        Ambiente sala = ambienteService.cadastrar(TipoAmbiente.MUSCULACAO, null);

        assertThat(sala.getId()).isEqualTo(101);
        assertThat(sala.getNome()).isEqualTo("Sala de Musculação 101");
        assertThat(sala.getValorHora()).isEqualTo(100.0);
    }

    @Test
    void idsSeguemEmSequenciaDentroDaFaixaDoTipo() {
        Ambiente primeira = ambienteService.cadastrar(TipoAmbiente.YOGA, null);
        Ambiente segunda = ambienteService.cadastrar(TipoAmbiente.YOGA, null);

        assertThat(primeira.getId()).isEqualTo(201);
        assertThat(segunda.getId()).isEqualTo(202);
    }

    @Test
    void deveLancarExcecaoQuandoAFaixaDoTipoEsgotar() {
        for (int i = 0; i < 10; i++) {
            ambienteService.cadastrar(TipoAmbiente.PISCINA, null); // 401 a 410
        }

        assertThatThrownBy(() -> ambienteService.cadastrar(TipoAmbiente.PISCINA, null))
                .isInstanceOf(LimiteAmbienteExcedidoException.class)
                .hasMessageContaining("401-410");
    }

    @Test
    void deveAtualizarSomenteAsObservacoes() {
        Ambiente sala = ambienteService.cadastrar(TipoAmbiente.CROSSFIT, "Antes");

        Ambiente atualizada = ambienteService.atualizarObservacoes(sala.getId(), "Depois");

        assertThat(atualizada.getObservacoes()).isEqualTo("Depois");
        assertThat(atualizada.getNome()).isEqualTo("Sala de Crossfit 301");
    }
}