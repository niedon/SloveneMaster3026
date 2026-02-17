package com.bcadaval.esloveno.beans;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para Variable - conversión segura de tipos desde String.
 */
@DisplayName("Variable")
class VariableTest {

    @Nested
    @DisplayName("getValorAsInteger()")
    class GetValorAsInteger {

        @Test
        @DisplayName("convierte correctamente un entero válido")
        void convierteEnteroValido() {
            Variable variable = Variable.builder().valor("42").build();
            assertThat(variable.getValorAsInteger()).isEqualTo(42);
        }

        @Test
        @DisplayName("maneja números negativos")
        void manejaNegativos() {
            Variable variable = Variable.builder().valor("-100").build();
            assertThat(variable.getValorAsInteger()).isEqualTo(-100);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("retorna null para valores vacíos o null")
        void retornaNullParaVaciosONull(String valor) {
            Variable variable = Variable.builder().valor(valor).build();
            assertThat(variable.getValorAsInteger()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "12.5", "123abc", "NaN"})
        @DisplayName("retorna null para valores no parseables")
        void retornaNullParaNoParseables(String valor) {
            Variable variable = Variable.builder().valor(valor).build();
            assertThat(variable.getValorAsInteger()).isNull();
        }

        @Test
        @DisplayName("ignora espacios alrededor del número")
        void ignoraEspacios() {
            Variable variable = Variable.builder().valor("  123  ").build();
            assertThat(variable.getValorAsInteger()).isEqualTo(123);
        }
    }

    @Nested
    @DisplayName("getValorAsLong()")
    class GetValorAsLong {

        @Test
        @DisplayName("convierte correctamente un long válido")
        void convierteLongValido() {
            Variable variable = Variable.builder().valor("9223372036854775807").build();
            assertThat(variable.getValorAsLong()).isEqualTo(Long.MAX_VALUE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("retorna null para valores vacíos o null")
        void retornaNullParaVaciosONull(String valor) {
            Variable variable = Variable.builder().valor(valor).build();
            assertThat(variable.getValorAsLong()).isNull();
        }

        @Test
        @DisplayName("retorna null para valores no parseables")
        void retornaNullParaNoParseables() {
            Variable variable = Variable.builder().valor("not-a-number").build();
            assertThat(variable.getValorAsLong()).isNull();
        }
    }

    @Nested
    @DisplayName("getValorAsDouble()")
    class GetValorAsDouble {

        @ParameterizedTest
        @CsvSource({
            "3.14, 3.14",
            "-2.5, -2.5",
            "100, 100.0",
            "0.0, 0.0"
        })
        @DisplayName("convierte correctamente doubles válidos")
        void convierteDoublesValidos(String valor, double esperado) {
            Variable variable = Variable.builder().valor(valor).build();
            assertThat(variable.getValorAsDouble()).isEqualTo(esperado);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("retorna null para valores vacíos o null")
        void retornaNullParaVaciosONull(String valor) {
            Variable variable = Variable.builder().valor(valor).build();
            assertThat(variable.getValorAsDouble()).isNull();
        }

        @Test
        @DisplayName("retorna null para valores no parseables")
        void retornaNullParaNoParseables() {
            Variable variable = Variable.builder().valor("abc").build();
            assertThat(variable.getValorAsDouble()).isNull();
        }
    }

    @Nested
    @DisplayName("getValorAsBoolean()")
    class GetValorAsBoolean {

        @ParameterizedTest
        @ValueSource(strings = {"true", "TRUE", "True", "TrUe"})
        @DisplayName("retorna true para variantes de 'true'")
        void retornaTrueParaTrue(String valor) {
            Variable variable = Variable.builder().valor(valor).build();
            assertThat(variable.getValorAsBoolean()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"false", "FALSE", "False", "yes", "no", "1", "0", "abc"})
        @DisplayName("retorna false para todo lo que no sea 'true'")
        void retornaFalseParaOtros(String valor) {
            Variable variable = Variable.builder().valor(valor).build();
            assertThat(variable.getValorAsBoolean()).isFalse();
        }

        @Test
        @DisplayName("retorna null para valor null")
        void retornaNullParaNull() {
            Variable variable = Variable.builder().valor(null).build();
            assertThat(variable.getValorAsBoolean()).isNull();
        }

        @Test
        @DisplayName("ignora espacios alrededor")
        void ignoraEspacios() {
            Variable variable = Variable.builder().valor("  true  ").build();
            assertThat(variable.getValorAsBoolean()).isTrue();
        }
    }
}
