package com.bcadaval.esloveno.beans.enums;

import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Getter;

/**
 * Enum que representa las formas verbales en esloveno.
 * INFINITIVE: Forma infinitiva del verbo (gledati)
 * SUPINE: Forma supina (gledat)
 * PARTICIPLE: Participio (gledal, gledala, etc.)
 * PRESENT: Presente (gledam, gledaš, etc.)
 * IMPERATIVE: Imperativo (glej, glejte, etc.)
 */
@Getter
public enum FormaVerbal {
    INFINITIVE("I", "infinitive"),
    SUPINE("S", "supine"),
    PARTICIPLE("P", "participle"),
    PRESENT("R", "present"),
    IMPERATIVE("M", "imperative");

    private final String code;
    private final String xmlCode;

    FormaVerbal(String code, String xmlCode) {
        this.code = code;
        this.xmlCode = xmlCode;
    }

    public static FormaVerbal fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (FormaVerbal forma : values()) {
            if (forma.code.equals(code) || forma.xmlCode.equals(code)) {
                return forma;
            }
        }
        return null;
    }

    public static FormaVerbal fromXmlCode(String xmlCode) {
        return fromCode(xmlCode);
    }

    @Converter(autoApply = true)
    public static class FormaVerbalConverter implements AttributeConverter<FormaVerbal, String> {

        @Override
        public String convertToDatabaseColumn(FormaVerbal attribute) {
            return Optional.ofNullable(attribute).map(FormaVerbal::getCode).orElse(null);
        }

        @Override
        public FormaVerbal convertToEntityAttribute(String dbData) {
            return FormaVerbal.fromCode(dbData);
        }
    }
}

