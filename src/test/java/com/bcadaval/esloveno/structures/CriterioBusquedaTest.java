package com.bcadaval.esloveno.structures;

import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para CriterioBusqueda - wrapper de CriterioGramatical con verificación de tipo.
 */
@DisplayName("CriterioBusqueda")
class CriterioBusquedaTest {

    @Nested
    @DisplayName("cumple()")
    class Cumple {

        @Test
        @DisplayName("retorna false para palabra null")
        void retornaFalseParaPalabraNull() {
            CriterioBusqueda<SustantivoFlexion> criterio = CriterioBusqueda.de(SustantivoFlexion.class)
                    .con(CaracteristicaGramatical.CASO, Caso.NOMINATIVO)
                    .build();

            assertThat(criterio.cumple(null)).isFalse();
        }

        @Test
        @DisplayName("retorna false si el tipo de flexión no coincide")
        void retornaFalseSiTipoNoCoincide() {
            CriterioBusqueda<VerboFlexion> criterio = CriterioBusqueda.de(VerboFlexion.class)
                    .con(CaracteristicaGramatical.FORMA_VERBAL, FormaVerbal.PRESENT)
                    .build();

            SustantivoFlexion sustantivo = SustantivoFlexion.builder()
                    .caso(Caso.NOMINATIVO)
                    .build();

            assertThat(criterio.cumple(sustantivo)).isFalse();
        }

        @Test
        @DisplayName("retorna true si cumple tipo y criterios")
        void retornaTrueSiCumpleTipoYCriterios() {
            CriterioBusqueda<SustantivoFlexion> criterio = CriterioBusqueda.de(SustantivoFlexion.class)
                    .con(CaracteristicaGramatical.CASO, Caso.GENITIVO)
                    .con(CaracteristicaGramatical.NUMERO, Numero.PLURAL)
                    .build();

            SustantivoFlexion sustantivo = SustantivoFlexion.builder()
                    .caso(Caso.GENITIVO)
                    .numero(Numero.PLURAL)
                    .build();

            assertThat(criterio.cumple(sustantivo)).isTrue();
        }

        @Test
        @DisplayName("delega correctamente al CriterioGramatical interno")
        void delegaAlCriterioGramatical() {
            CriterioBusqueda<SustantivoFlexion> criterio = CriterioBusqueda.de(SustantivoFlexion.class)
                    .con(CaracteristicaGramatical.CASO, Caso.LOCATIVO)
                    .build();

            SustantivoFlexion cumple = SustantivoFlexion.builder().caso(Caso.LOCATIVO).build();
            SustantivoFlexion noCumple = SustantivoFlexion.builder().caso(Caso.DATIVO).build();

            assertThat(criterio.cumple(cumple)).isTrue();
            assertThat(criterio.cumple(noCumple)).isFalse();
        }
    }

    @Nested
    @DisplayName("getTipoFlexion()")
    class GetTipoFlexion {

        @Test
        @DisplayName("retorna la clase de tipo correcta")
        void retornaClaseCorrecta() {
            CriterioBusqueda<SustantivoFlexion> criterio = CriterioBusqueda.de(SustantivoFlexion.class)
                    .build();

            assertThat(criterio.getTipoFlexion()).isEqualTo(SustantivoFlexion.class);
        }
    }

    @Nested
    @DisplayName("getCriterioGramatical()")
    class GetCriterioGramatical {

        @Test
        @DisplayName("retorna el criterio gramatical configurado")
        void retornaCriterioGramatical() {
            CriterioBusqueda<SustantivoFlexion> criterio = CriterioBusqueda.de(SustantivoFlexion.class)
                    .con(CaracteristicaGramatical.CASO, Caso.INSTRUMENTAL)
                    .build();

            CriterioGramatical criterioGramatical = criterio.getCriterioGramatical();

            assertThat(criterioGramatical).isNotNull();
            assertThat(criterioGramatical.getRequisitos())
                    .containsEntry(CaracteristicaGramatical.CASO, Caso.INSTRUMENTAL);
        }
    }
}
