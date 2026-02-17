package com.bcadaval.esloveno.beans.enums;

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
 * Tests para TipoPalabra - conversiones entre códigos XML y clases.
 */
@DisplayName("TipoPalabra")
class TipoPalabraTest {

    @Nested
    @DisplayName("fromXmlCode()")
    class FromXmlCode {

        @ParameterizedTest
        @CsvSource({
            "noun, SUSTANTIVO",
            "verb, VERBO",
            "adjective, ADJETIVO",
            "pronoun, PRONOMBRE",
            "numeral, NUMERAL"
        })
        @DisplayName("convierte códigos XML válidos correctamente")
        void convierteCodigosValidos(String xmlCode, String expectedName) {
            TipoPalabra resultado = TipoPalabra.fromXmlCode(xmlCode);

            assertThat(resultado).isNotNull();
            assertThat(resultado.name()).isEqualTo(expectedName);
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "invalid", "NOUN", "Noun", "adverb"})
        @DisplayName("retorna null para códigos inválidos o null")
        void retornaNullParaInvalidos(String xmlCode) {
            assertThat(TipoPalabra.fromXmlCode(xmlCode)).isNull();
        }
    }

    @Nested
    @DisplayName("fromClass()")
    class FromClass {

        @Test
        @DisplayName("encuentra tipo para clase de palabra principal")
        void encuentraTipoParaClasePrincipal() {
            assertThat(TipoPalabra.fromClass(Sustantivo.class)).isEqualTo(TipoPalabra.SUSTANTIVO);
            assertThat(TipoPalabra.fromClass(Verbo.class)).isEqualTo(TipoPalabra.VERBO);
            assertThat(TipoPalabra.fromClass(Adjetivo.class)).isEqualTo(TipoPalabra.ADJETIVO);
            assertThat(TipoPalabra.fromClass(Pronombre.class)).isEqualTo(TipoPalabra.PRONOMBRE);
            assertThat(TipoPalabra.fromClass(Numeral.class)).isEqualTo(TipoPalabra.NUMERAL);
        }

        @Test
        @DisplayName("retorna null para clases no reconocidas")
        void retornaNullParaClasesNoReconocidas() {
            assertThat(TipoPalabra.fromClass(String.class)).isNull();
            assertThat(TipoPalabra.fromClass(null)).isNull();
        }
    }

    @Nested
    @DisplayName("fromFlexionClass()")
    class FromFlexionClass {

        @Test
        @DisplayName("encuentra tipo para clase de flexión")
        void encuentraTipoParaClaseFlexion() {
            assertThat(TipoPalabra.fromFlexionClass(SustantivoFlexion.class)).isEqualTo(TipoPalabra.SUSTANTIVO);
            assertThat(TipoPalabra.fromFlexionClass(VerboFlexion.class)).isEqualTo(TipoPalabra.VERBO);
            assertThat(TipoPalabra.fromFlexionClass(AdjetivoFlexion.class)).isEqualTo(TipoPalabra.ADJETIVO);
            assertThat(TipoPalabra.fromFlexionClass(PronombreFlexion.class)).isEqualTo(TipoPalabra.PRONOMBRE);
            assertThat(TipoPalabra.fromFlexionClass(NumeralFlexion.class)).isEqualTo(TipoPalabra.NUMERAL);
        }

        @Test
        @DisplayName("retorna null para clases no reconocidas")
        void retornaNullParaClasesNoReconocidas() {
            assertThat(TipoPalabra.fromFlexionClass(Object.class)).isNull();
            assertThat(TipoPalabra.fromFlexionClass(null)).isNull();
        }
    }

    @Nested
    @DisplayName("getXmlCode()")
    class GetXmlCode {

        @Test
        @DisplayName("cada tipo tiene su código XML correcto")
        void cadaTipoTieneCodigoXml() {
            assertThat(TipoPalabra.SUSTANTIVO.getXmlCode()).isEqualTo("noun");
            assertThat(TipoPalabra.VERBO.getXmlCode()).isEqualTo("verb");
            assertThat(TipoPalabra.ADJETIVO.getXmlCode()).isEqualTo("adjective");
            assertThat(TipoPalabra.PRONOMBRE.getXmlCode()).isEqualTo("pronoun");
            assertThat(TipoPalabra.NUMERAL.getXmlCode()).isEqualTo("numeral");
        }
    }

    @Nested
    @DisplayName("Consistencia bidireccional")
    class ConsistenciaBidireccional {

        @Test
        @DisplayName("fromXmlCode y getXmlCode son inversos")
        void fromXmlCodeYGetXmlCodeSonInversos() {
            for (TipoPalabra tipo : TipoPalabra.values()) {
                String xmlCode = tipo.getXmlCode();
                TipoPalabra resultado = TipoPalabra.fromXmlCode(xmlCode);

                assertThat(resultado)
                    .as("TipoPalabra.fromXmlCode('%s') debería retornar %s", xmlCode, tipo)
                    .isEqualTo(tipo);
            }
        }

        @Test
        @DisplayName("fromClass y getClazz son inversos")
        void fromClassYGetClazzSonInversos() {
            for (TipoPalabra tipo : TipoPalabra.values()) {
                Class<?> clazz = tipo.getClazz();
                TipoPalabra resultado = TipoPalabra.fromClass(clazz);

                assertThat(resultado)
                    .as("TipoPalabra.fromClass(%s) debería retornar %s", clazz.getSimpleName(), tipo)
                    .isEqualTo(tipo);
            }
        }
    }
}
