package com.bcadaval.esloveno.config;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convertidor personalizado para mapear Instant a DATETIME en SQLite.
 * Trunca milisegundos (precisión de segundo) y usa formato YYYY-MM-DD HH:MM:SS.
 * SQLite DATETIME no soporta milisegundos nativamente.
 */
@Converter(autoApply = true)
public class InstantConverter implements AttributeConverter<Instant, String> {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(ZoneOffset.UTC);

    /**
     * Convierte Instant a String formato DATETIME de SQLite.
     * Trunca milisegundos a precisión de segundo.
     */
    @Override
    public String convertToDatabaseColumn(Instant attribute) {
        if (attribute == null) {
            return null;
        }
        // Truncar a segundos (SQLite DATETIME no soporta milisegundos)
        Instant truncated = attribute.truncatedTo(ChronoUnit.SECONDS);
        return FORMATTER.format(truncated);
    }

    /**
     * Convierte String DATETIME de SQLite a Instant.
     * Asume formato UTC.
     */
    @Override
    public Instant convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            // Parsear formato DATETIME de SQLite
            return Instant.parse(dbData.replace(" ", "T") + "Z");
        } catch (Exception e) {
            // Fallback: intentar parsear como ISO-8601
            return Instant.parse(dbData);
        }
    }
}

