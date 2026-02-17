package com.bcadaval.esloveno.structures;

import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Genero;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.palabra.Sustantivo;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para CriterioGramatical - lógica central de filtrado de palabras.
 */
@DisplayName("CriterioGramatical")
class CriterioGramaticalTest {

    @Nested
    @DisplayName("cumple()")
    class Cumple {

        @Test
        @DisplayName("retorna false para palabra null")
        void retornaFalseParaPalabraNull() {
            CriterioGramatical criterio = CriterioGramatical.de(SustantivoFlexion.class)
                    .con(CaracteristicaGramatical.CASO, Caso.NOMINATIVO)
                    .build();

            assertThat(criterio.cumple(null)).isFalse();
        }

        @Test
        @DisplayName("retorna false si el tipo de flexión no coincide")
        void retornaFalseSiTipoNoCoincide() {
            CriterioGramatical criterio = CriterioGramatical.de(VerboFlexion.class)
                    .build();

            SustantivoFlexion sustantivo = SustantivoFlexion.builder()
                    .caso(Caso.NOMINATIVO)
                    .numero(Numero.SINGULAR)
                    .build();

            assertThat(criterio.cumple(sustantivo)).isFalse();
        }

        @Test
        @DisplayName("retorna true si cumple todos los requisitos")
        void retornaTrueSiCumpleTodosLosRequisitos() {
            CriterioGramatical criterio = CriterioGramatical.de(SustantivoFlexion.class)
                    .con(CaracteristicaGramatical.CASO, Caso.ACUSATIVO)
                    .con(CaracteristicaGramatical.NUMERO, Numero.PLURAL)
                    .build();

            SustantivoFlexion sustantivo = SustantivoFlexion.builder()
                    .caso(Caso.ACUSATIVO)
                    .numero(Numero.PLURAL)
                    .build();

            assertThat(criterio.cumple(sustantivo)).isTrue();
        }

        @Test
        @DisplayName("retorna false si no cumple algún requisito")
        void retornaFalseSiNoCumpleAlgunRequisito() {
            CriterioGramatical criterio = CriterioGramatical.de(SustantivoFlexion.class)
                    .con(CaracteristicaGramatical.CASO, Caso.ACUSATIVO)
                    .con(CaracteristicaGramatical.NUMERO, Numero.PLURAL)
                    .build();

            SustantivoFlexion sustantivo = SustantivoFlexion.builder()
                    .caso(Caso.ACUSATIVO)
                    .numero(Numero.SINGULAR)  // No coincide
                    .build();

            assertThat(criterio.cumple(sustantivo)).isFalse();
        }

        @Test
        @DisplayName("retorna true sin requisitos si el tipo coincide")
        void retornaTrueSinRequisitosSiTipoCoincide() {
            CriterioGramatical criterio = CriterioGramatical.de(SustantivoFlexion.class)
                    .build();

            SustantivoFlexion sustantivo = SustantivoFlexion.builder()
                    .caso(Caso.NOMINATIVO)
                    .build();

            assertThat(criterio.cumple(sustantivo)).isTrue();
        }

        @Test
        @DisplayName("maneja correctamente características de la palabra base")
        void manejaCaracteristicasDePalabraBase() {
            Sustantivo sustantivoBase = Sustantivo.builder()
                    .genero(Genero.MASCULINO)
                    .significado("perro")
                    .build();

            SustantivoFlexion flexion = SustantivoFlexion.builder()
                    .caso(Caso.NOMINATIVO)
                    .numero(Numero.SINGULAR)
                    .sustantivoBase(sustantivoBase)
                    .build();

            CriterioGramatical criterio = CriterioGramatical.de(SustantivoFlexion.class)
                    .con(CaracteristicaGramatical.GENERO, Genero.MASCULINO)
                    .build();

            assertThat(criterio.cumple(flexion)).isTrue();
        }

        @Test
        @DisplayName("retorna false si género no coincide")
        void retornaFalseSiGeneroNoCoincide() {
            Sustantivo sustantivoBase = Sustantivo.builder()
                    .genero(Genero.FEMENINO)
                    .significado("casa")
                    .build();

            SustantivoFlexion flexion = SustantivoFlexion.builder()
                    .caso(Caso.NOMINATIVO)
                    .sustantivoBase(sustantivoBase)
                    .build();

            CriterioGramatical criterio = CriterioGramatical.de(SustantivoFlexion.class)
                    .con(CaracteristicaGramatical.GENERO, Genero.MASCULINO)
                    .build();

            assertThat(criterio.cumple(flexion)).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTest {

        @Test
        @DisplayName("permite encadenar múltiples requisitos")
        void permiteEncadenarMultiplesRequisitos() {
            CriterioGramatical criterio = CriterioGramatical.de(SustantivoFlexion.class)
                    .con(CaracteristicaGramatical.CASO, Caso.DATIVO)
                    .con(CaracteristicaGramatical.NUMERO, Numero.DUAL)
                    .build();

            assertThat(criterio.getRequisitos())
                    .containsEntry(CaracteristicaGramatical.CASO, Caso.DATIVO)
                    .containsEntry(CaracteristicaGramatical.NUMERO, Numero.DUAL);
        }

        @Test
        @DisplayName("sobrescribe requisitos duplicados")
        void sobrescribeRequisitosDuplicados() {
            CriterioGramatical criterio = CriterioGramatical.de(SustantivoFlexion.class)
                    .con(CaracteristicaGramatical.CASO, Caso.NOMINATIVO)
                    .con(CaracteristicaGramatical.CASO, Caso.GENITIVO) // Sobrescribe
                    .build();

            assertThat(criterio.getRequisitos())
                    .containsEntry(CaracteristicaGramatical.CASO, Caso.GENITIVO)
                    .hasSize(1);
        }
    }
}
