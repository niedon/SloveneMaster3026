package com.bcadaval.esloveno.beans.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum Definitud {
    INDEFINIDO("no", 0), DEFINIDO("yes", 1);

    private final String codigoXml;
    private final Integer codigoBd;

    Definitud(String codigoXml, Integer codigoBd) {
        this.codigoXml = codigoXml;
        this.codigoBd = codigoBd;
    }

    public String getCodigoXml() {
        return codigoXml;
    }

    public Integer getCodigoBd() {
        return codigoBd;
    }

    public static Definitud fromCodigoXml(String codigoXml) {
        if (codigoXml == null || codigoXml.isBlank()) {
            return null;
        }
        for (Definitud def : values()) {
            if (def.codigoXml.equals(codigoXml)) {
                return def;
            }
        }
        return null;
    }

    public static Definitud fromCodigoBd(Integer codigoBd) {
        if (codigoBd == null) {
            return null;
        }
        for (Definitud def : values()) {
            if (def.codigoBd.equals(codigoBd)) {
                return def;
            }
        }
        return null;
    }

    @Converter(autoApply = true)
    public static class DefinitudConverter implements AttributeConverter<Definitud, Integer> {
        @Override
        public Integer convertToDatabaseColumn(Definitud attribute) {
            return attribute != null ? attribute.getCodigoBd() : null;
        }

        @Override
        public Definitud convertToEntityAttribute(Integer dbData) {
            return Definitud.fromCodigoBd(dbData);
        }
    }
}
