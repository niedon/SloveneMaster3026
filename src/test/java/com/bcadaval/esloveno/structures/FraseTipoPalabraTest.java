package com.bcadaval.esloveno.structures;

import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.palabra.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para FraseTipoPalabra - mapeo entre objetos y códigos de tipo.
 */
@DisplayName("FraseTipoPalabra")
class FraseTipoPalabraTest {

    @Nested
    @DisplayName("fromObject()")
    class FromObject {

        @Test
        @DisplayName("identifica VerboFlexion correctamente")
        void identificaVerboFlexion() {
            VerboFlexion verbo = VerboFlexion.builder().build();

            assertThat(FraseTipoPalabra.fromObject(verbo))
                    .isEqualTo(FraseTipoPalabra.VERBO_FLEXION);
        }

        @Test
        @DisplayName("identifica SustantivoFlexion correctamente")
        void identificaSustantivoFlexion() {
            SustantivoFlexion sustantivo = SustantivoFlexion.builder().build();

            assertThat(FraseTipoPalabra.fromObject(sustantivo))
                    .isEqualTo(FraseTipoPalabra.SUSTANTIVO_FLEXION);
        }

        @Test
        @DisplayName("identifica AdjetivoFlexion correctamente")
        void identificaAdjetivoFlexion() {
            AdjetivoFlexion adjetivo = AdjetivoFlexion.builder().build();

            assertThat(FraseTipoPalabra.fromObject(adjetivo))
                    .isEqualTo(FraseTipoPalabra.ADJETIVO_FLEXION);
        }

        @Test
        @DisplayName("identifica Numero correctamente")
        void identificaNumero() {
            assertThat(FraseTipoPalabra.fromObject(Numero.SINGULAR))
                    .isEqualTo(FraseTipoPalabra.NUMERO);
        }

        @Test
        @DisplayName("retorna null para objeto null")
        void retornaNullParaNull() {
            assertThat(FraseTipoPalabra.fromObject(null)).isNull();
        }

        @Test
        @DisplayName("retorna null para objetos no reconocidos")
        void retornaNullParaNoReconocidos() {
            assertThat(FraseTipoPalabra.fromObject("string")).isNull();
            assertThat(FraseTipoPalabra.fromObject(123)).isNull();
        }
    }

    @Nested
    @DisplayName("fromCodigo()")
    class FromCodigo {

        @ParameterizedTest
        @CsvSource({
            "v, VERBO_FLEXION",
            "s, SUSTANTIVO_FLEXION",
            "a, ADJETIVO_FLEXION",
            "p, PRONOMBRE",
            "n, NUMERO"
        })
        @DisplayName("convierte códigos válidos correctamente")
        void convierteCodigosValidos(String codigo, String expectedName) {
            FraseTipoPalabra resultado = FraseTipoPalabra.fromCodigo(codigo);

            assertThat(resultado).isNotNull();
            assertThat(resultado.name()).isEqualTo(expectedName);
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "x", "V", "verbo", "123"})
        @DisplayName("retorna null para códigos inválidos")
        void retornaNullParaInvalidos(String codigo) {
            assertThat(FraseTipoPalabra.fromCodigo(codigo)).isNull();
        }
    }

    @Nested
    @DisplayName("getCodigo()")
    class GetCodigo {

        @Test
        @DisplayName("cada tipo tiene su código correcto")
        void cadaTipoTieneCodigo() {
            assertThat(FraseTipoPalabra.VERBO_FLEXION.getCodigo()).isEqualTo("v");
            assertThat(FraseTipoPalabra.SUSTANTIVO_FLEXION.getCodigo()).isEqualTo("s");
            assertThat(FraseTipoPalabra.ADJETIVO_FLEXION.getCodigo()).isEqualTo("a");
            assertThat(FraseTipoPalabra.PRONOMBRE.getCodigo()).isEqualTo("p");
            assertThat(FraseTipoPalabra.NUMERO.getCodigo()).isEqualTo("n");
        }
    }

    @Nested
    @DisplayName("Consistencia")
    class Consistencia {

        @Test
        @DisplayName("fromCodigo y getCodigo son inversos")
        void fromCodigoYGetCodigoSonInversos() {
            for (FraseTipoPalabra tipo : FraseTipoPalabra.values()) {
                String codigo = tipo.getCodigo();
                FraseTipoPalabra resultado = FraseTipoPalabra.fromCodigo(codigo);

                assertThat(resultado)
                    .as("FraseTipoPalabra.fromCodigo('%s') debería retornar %s", codigo, tipo)
                    .isEqualTo(tipo);
            }
        }

        @Test
        @DisplayName("todos los códigos son únicos")
        void todosLosCodigosSonUnicos() {
            FraseTipoPalabra[] tipos = FraseTipoPalabra.values();

            for (int i = 0; i < tipos.length; i++) {
                for (int j = i + 1; j < tipos.length; j++) {
                    assertThat(tipos[i].getCodigo())
                            .as("Códigos de %s y %s no deben ser iguales", tipos[i], tipos[j])
                            .isNotEqualTo(tipos[j].getCodigo());
                }
            }
        }
    }
}
