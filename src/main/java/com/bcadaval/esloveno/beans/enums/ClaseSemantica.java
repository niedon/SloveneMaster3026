package com.bcadaval.esloveno.beans.enums;

import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Getter;

/**
 * Clase semántica de un sustantivo.
 * Clasifica el sustantivo según su naturaleza conceptual.
 */
@Getter
public enum ClaseSemantica {

    HUMANO("HU"),
    ANIMAL("AN"),
    OBJETO("OB"),
    LUGAR("LU"),
    SUSTANCIA("SU"),
    ABSTRACTO("AB");

    private final String code;

    ClaseSemantica(String code) {
        this.code = code;
    }

    public static ClaseSemantica fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return switch (code) {
            case "HU" -> HUMANO;
            case "AN" -> ANIMAL;
            case "OB" -> OBJETO;
            case "LU" -> LUGAR;
            case "SU" -> SUSTANCIA;
            case "AB" -> ABSTRACTO;
            default -> null;
        };
    }

    @Converter(autoApply = true)
    public static class ClaseSemanticaConverter implements AttributeConverter<ClaseSemantica, String> {

        @Override
        public String convertToDatabaseColumn(ClaseSemantica attribute) {
            return Optional.ofNullable(attribute).map(ClaseSemantica::getCode).orElse(null);
        }

        @Override
        public ClaseSemantica convertToEntityAttribute(String dbData) {
            return ClaseSemantica.fromCode(dbData);
        }
    }
}

