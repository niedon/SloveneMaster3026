package com.bcadaval.esloveno.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para InstantConverter - conversión entre Instant y String DATETIME de SQLite.
 * Crítico para la persistencia correcta de fechas del SRS.
 */
@DisplayName("InstantConverter")
class InstantConverterTest {

    private final InstantConverter converter = new InstantConverter();

    @Nested
    @DisplayName("convertToDatabaseColumn()")
    class ConvertToDatabaseColumn {

        @Test
        @DisplayName("retorna null para Instant null")
        void retornaNullParaNull() {
            assertThat(converter.convertToDatabaseColumn(null)).isNull();
        }

        @Test
        @DisplayName("convierte Instant a formato DATETIME correcto")
        void convierteAFormato() {
            Instant instant = Instant.parse("2024-06-15T14:30:45Z");

            String resultado = converter.convertToDatabaseColumn(instant);

            assertThat(resultado).isEqualTo("2024-06-15 14:30:45");
        }

        @Test
        @DisplayName("trunca milisegundos a segundos")
        void truncaMillisegundos() {
            Instant instant = Instant.parse("2024-06-15T14:30:45.789Z");

            String resultado = converter.convertToDatabaseColumn(instant);

            // Debe perder los milisegundos
            assertThat(resultado).isEqualTo("2024-06-15 14:30:45");
        }

        @Test
        @DisplayName("maneja correctamente el inicio de época")
        void manejaInicioEpoca() {
            Instant epoch = Instant.EPOCH;

            String resultado = converter.convertToDatabaseColumn(epoch);

            assertThat(resultado).isEqualTo("1970-01-01 00:00:00");
        }

        @Test
        @DisplayName("maneja fechas en el futuro")
        void manejaFechasFuturo() {
            Instant futuro = Instant.parse("2099-12-31T23:59:59Z");

            String resultado = converter.convertToDatabaseColumn(futuro);

            assertThat(resultado).isEqualTo("2099-12-31 23:59:59");
        }
    }

    @Nested
    @DisplayName("convertToEntityAttribute()")
    class ConvertToEntityAttribute {

        @Test
        @DisplayName("retorna null para String null")
        void retornaNullParaNull() {
            assertThat(converter.convertToEntityAttribute(null)).isNull();
        }

        @Test
        @DisplayName("retorna null para String vacío")
        void retornaNullParaVacio() {
            assertThat(converter.convertToEntityAttribute("")).isNull();
        }

        @Test
        @DisplayName("parsea formato DATETIME de SQLite")
        void parseaFormatoSQLite() {
            String dbData = "2024-06-15 14:30:45";

            Instant resultado = converter.convertToEntityAttribute(dbData);

            assertThat(resultado).isEqualTo(Instant.parse("2024-06-15T14:30:45Z"));
        }

        @Test
        @DisplayName("parsea formato ISO-8601 estándar como fallback")
        void parseaFormatoISO() {
            String isoData = "2024-06-15T14:30:45Z";

            Instant resultado = converter.convertToEntityAttribute(isoData);

            assertThat(resultado).isEqualTo(Instant.parse("2024-06-15T14:30:45Z"));
        }
    }

    @Nested
    @DisplayName("Roundtrip (ida y vuelta)")
    class Roundtrip {

        @Test
        @DisplayName("conversión ida y vuelta preserva el valor (sin milisegundos)")
        void roundtripPreservaValor() {
            Instant original = Instant.parse("2024-06-15T14:30:45Z");

            String enDb = converter.convertToDatabaseColumn(original);
            Instant recuperado = converter.convertToEntityAttribute(enDb);

            assertThat(recuperado).isEqualTo(original);
        }

        @Test
        @DisplayName("roundtrip pierde precisión de milisegundos (esperado)")
        void roundtripPierdeMillisegundos() {
            Instant conMillis = Instant.parse("2024-06-15T14:30:45.999Z");
            Instant sinMillis = Instant.parse("2024-06-15T14:30:45Z");

            String enDb = converter.convertToDatabaseColumn(conMillis);
            Instant recuperado = converter.convertToEntityAttribute(enDb);

            // El original tenía milisegundos, el recuperado no
            assertThat(recuperado).isEqualTo(sinMillis);
            assertThat(recuperado).isNotEqualTo(conMillis);
        }
    }
}
