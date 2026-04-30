package com.bcadaval.esloveno.beans.enums;

import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Getter;

/**
 * Indica si un verbo requiere que su sujeto sea animado.
 * <ul>
 *   <li>{@code SI} — Requiere sujeto animado</li>
 *   <li>{@code NO} — No requiere sujeto animado</li>
 * </ul>
 */
@Getter
public enum RequiereSujetoAnimado {

    SI("S"),
    NO("N");

    private final String code;

    RequiereSujetoAnimado(String code) {
        this.code = code;
    }

    public static RequiereSujetoAnimado fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return switch (code) {
            case "S" -> SI;
            case "N" -> NO;
            default -> null;
        };
    }

    @SuppressWarnings("unused")
    @Converter(autoApply = true)
    public static class RequiereSujetoAnimadoConverter implements AttributeConverter<RequiereSujetoAnimado, String> {

        @Override
        public String convertToDatabaseColumn(RequiereSujetoAnimado attribute) {
            return Optional.ofNullable(attribute).map(RequiereSujetoAnimado::getCode).orElse(null);
        }

        @Override
        public RequiereSujetoAnimado convertToEntityAttribute(String dbData) {
            return RequiereSujetoAnimado.fromCode(dbData);
        }
    }
}

