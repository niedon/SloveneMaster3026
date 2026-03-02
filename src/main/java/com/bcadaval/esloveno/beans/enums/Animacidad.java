package com.bcadaval.esloveno.beans.enums;

import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Getter;

/**
 * Animacidad de un sustantivo.
 * <ul>
 *   <li>{@code ANIMADO} — Seres vivos (equivalente al antiguo {@code animado = true})</li>
 *   <li>{@code INANIMADO} — Objetos inanimados (equivalente al antiguo {@code animado = false})</li>
 * </ul>
 */
@Getter
public enum Animacidad {

    ANIMADO("A"),
    INANIMADO("I");

    private final String code;

    Animacidad(String code) {
        this.code = code;
    }

    public static Animacidad fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return switch (code) {
            case "A" -> ANIMADO;
            case "I" -> INANIMADO;
            default -> null;
        };
    }

    /**
     * Convierte desde el antiguo campo {@code Boolean animado} al nuevo enum.
     *
     * @param animado valor booleano antiguo (puede ser null)
     * @return {@code ANIMADO} si true, {@code INANIMADO} si false, {@code null} si null
     */
    public static Animacidad fromBoolean(Boolean animado) {
        if (animado == null) return null;
        return animado ? ANIMADO : INANIMADO;
    }

    @Converter(autoApply = true)
    public static class AnimacidadConverter implements AttributeConverter<Animacidad, String> {

        @Override
        public String convertToDatabaseColumn(Animacidad attribute) {
            return Optional.ofNullable(attribute).map(Animacidad::getCode).orElse(null);
        }

        @Override
        public Animacidad convertToEntityAttribute(String dbData) {
            return Animacidad.fromCode(dbData);
        }
    }
}

