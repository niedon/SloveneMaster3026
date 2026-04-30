package com.bcadaval.esloveno.beans.enums;

import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Getter;

/**
 * Indica si un verbo requiere que su objeto directo sea animado.
 * <ul>
 *   <li>{@code SI} — Requiere objeto animado</li>
 *   <li>{@code NO} — No requiere objeto animado</li>
 * </ul>
 */
@Getter
public enum RequiereObjetoAnimado {

    SI("S"),
    NO("N");

    private final String code;

    RequiereObjetoAnimado(String code) {
        this.code = code;
    }

    public static RequiereObjetoAnimado fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return switch (code) {
            case "S" -> SI;
            case "N" -> NO;
            default -> null;
        };
    }

    @SuppressWarnings("unused")
    @Converter(autoApply = true)
    public static class RequiereObjetoAnimadoConverter implements AttributeConverter<RequiereObjetoAnimado, String> {

        @Override
        public String convertToDatabaseColumn(RequiereObjetoAnimado attribute) {
            return Optional.ofNullable(attribute).map(RequiereObjetoAnimado::getCode).orElse(null);
        }

        @Override
        public RequiereObjetoAnimado convertToEntityAttribute(String dbData) {
            return RequiereObjetoAnimado.fromCode(dbData);
        }
    }
}

