package com.bcadaval.esloveno.beans.enums;

import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Getter;

/**
 * Indica si un sustantivo es cabeza relacional.
 * <ul>
 *   <li>{@code SI} — Es cabeza relacional</li>
 *   <li>{@code NO} — No es cabeza relacional</li>
 * </ul>
 */
@Getter
public enum CabezaRelacional {

    SI("S"),
    NO("N");

    private final String code;

    CabezaRelacional(String code) {
        this.code = code;
    }

    public static CabezaRelacional fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return switch (code) {
            case "S" -> SI;
            case "N" -> NO;
            default -> null;
        };
    }

    @Converter(autoApply = true)
    public static class CabezaRelacionalConverter implements AttributeConverter<CabezaRelacional, String> {

        @Override
        public String convertToDatabaseColumn(CabezaRelacional attribute) {
            return Optional.ofNullable(attribute).map(CabezaRelacional::getCode).orElse(null);
        }

        @Override
        public CabezaRelacional convertToEntityAttribute(String dbData) {
            return CabezaRelacional.fromCode(dbData);
        }
    }
}

