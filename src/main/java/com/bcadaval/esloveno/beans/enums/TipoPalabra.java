package com.bcadaval.esloveno.beans.enums;

import com.bcadaval.esloveno.beans.palabra.*;

import lombok.Getter;

/**
 * Enum que representa los tipos de palabras soportados en el sistema.
 * Cada tipo tiene su código XML, clase principal y clase de flexión asociadas.
 */
@Getter
public enum TipoPalabra {

    SUSTANTIVO("noun", Sustantivo.class, SustantivoFlexion.class),
    VERBO("verb", Verbo.class, VerboFlexion.class),
    ADJETIVO("adjective", Adjetivo.class, AdjetivoFlexion.class),
    PRONOMBRE("pronoun", Pronombre.class, PronombreFlexion.class),
    NUMERAL("numeral", Numeral.class, NumeralFlexion.class);

    private final String xmlCode;
    private final Class<?> clazz;
    private final Class<?> flexionClazz;

    TipoPalabra(String xmlCode, Class<?> clazz, Class<?> flexionClazz) {
        this.xmlCode = xmlCode;
        this.clazz = clazz;
        this.flexionClazz = flexionClazz;
    }

    /**
     * Obtiene el TipoPalabra a partir del código XML (category)
     * @param xmlCode Código del XML (noun, verb, adjective, etc.)
     * @return TipoPalabra correspondiente o null si no se encuentra
     */
    public static TipoPalabra fromXmlCode(String xmlCode) {
        if (xmlCode == null) {
            return null;
        }
        for (TipoPalabra tipo : values()) {
            if (tipo.xmlCode.equals(xmlCode)) {
                return tipo;
            }
        }
        return null;
    }

    public static TipoPalabra fromClass(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        for (TipoPalabra tipo : values()) {
            if (tipo.clazz.equals(clazz)) {
                return tipo;
            }
        }
        return null;
    }

    public static TipoPalabra fromFlexionClass(Class<?> flexionClazz) {
        if (flexionClazz == null) {
            return null;
        }
        for (TipoPalabra tipo : values()) {
            if (tipo.flexionClazz.equals(flexionClazz)) {
                return tipo;
            }
        }
        return null;
    }

    /**
     * Traduce el tipo de palabra al español
     */
    public String getNombreEspanol() {
        return switch (this) {
            case SUSTANTIVO -> "Sustantivo";
            case VERBO -> "Verbo";
            case ADJETIVO -> "Adjetivo";
            case PRONOMBRE -> "Pronombre";
            case NUMERAL -> "Numeral";
        };
    }

    /**
     * Verifica si el código XML está soportado
     */
    public static boolean isSoportado(String xmlCode) {
        return fromXmlCode(xmlCode) != null;
    }
}

