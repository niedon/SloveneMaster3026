package com.bcadaval.esloveno.structures;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para ModoVisualizacion - verificar aleatorización correcta.
 */
@DisplayName("ModoVisualizacion")
class ModoVisualizacionTest {

    @Test
    @DisplayName("aleatorio() retorna un valor válido del enum")
    void aleatorioRetornaValorValido() {
        ModoVisualizacion modo = ModoVisualizacion.aleatorio();

        assertThat(modo).isIn(ModoVisualizacion.ES_SL, ModoVisualizacion.SL_ES);
    }

    @RepeatedTest(10)
    @DisplayName("aleatorio() produce valores válidos en múltiples llamadas")
    void aleatorioProduceValoresValidos() {
        ModoVisualizacion modo = ModoVisualizacion.aleatorio();

        assertThat(modo).isNotNull();
        assertThat(EnumSet.allOf(ModoVisualizacion.class)).contains(modo);
    }

    @Test
    @DisplayName("aleatorio() eventualmente produce ambos valores (distribución)")
    void aleatorioProduceAmbosValores() {
        Set<ModoVisualizacion> modosObtenidos = EnumSet.noneOf(ModoVisualizacion.class);

        // Con suficientes iteraciones, deberíamos obtener ambos modos
        IntStream.range(0, 100).forEach(i ->
            modosObtenidos.add(ModoVisualizacion.aleatorio())
        );

        assertThat(modosObtenidos)
                .as("Después de 100 llamadas, deberían aparecer ambos modos")
                .containsExactlyInAnyOrder(ModoVisualizacion.ES_SL, ModoVisualizacion.SL_ES);
    }

    @Test
    @DisplayName("enum tiene exactamente dos valores")
    void enumTieneDosValores() {
        assertThat(ModoVisualizacion.values()).hasSize(2);
    }
}
