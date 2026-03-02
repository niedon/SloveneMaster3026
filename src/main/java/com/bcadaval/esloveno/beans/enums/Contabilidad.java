package com.bcadaval.esloveno.beans.enums;

import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Getter;

/**
 * Contabilidad de un sustantivo.
 * <ul>
 *   <li>{@code CONTABLE} — Se puede contar (libro, casa, persona)</li>
 *   <li>{@code INCONTABLE} — No se puede contar (agua, arena, felicidad)</li>
 * </ul>
 */
@Getter
public enum Contabilidad {

    CONTABLE("C"),
    INCONTABLE("I");

    private final String code;

    Contabilidad(String code) {
        this.code = code;
    }

    public static Contabilidad fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return switch (code) {
            case "C" -> CONTABLE;
            case "I" -> INCONTABLE;
            default -> null;
        };
    }

    @Converter(autoApply = true)
    public static class ContabilidadConverter implements AttributeConverter<Contabilidad, String> {

        @Override
        public String convertToDatabaseColumn(Contabilidad attribute) {
            return Optional.ofNullable(attribute).map(Contabilidad::getCode).orElse(null);
        }

        @Override
        public Contabilidad convertToEntityAttribute(String dbData) {
            return Contabilidad.fromCode(dbData);
        }
    }
}

