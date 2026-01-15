package com.bcadaval.esloveno.beans.enums;

import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Getter;

/**
 * Transitividad de un verbo
 * TRANSITIVO: requiere objeto directo
 * INTRANSITIVO: no requiere objeto directo
 * AMBITRANSITIVO: puede ser transitivo o intransitivo
 */
@Getter
public enum Transitividad {
	TRANSITIVO("T"), INTRANSITIVO("I"), AMBITRANSITIVO("A");

	private final String code;

	Transitividad(String code) {
		this.code = code;
	}

    public static Transitividad fromCode(String code) {
		return code==null ? null : switch (code) {
        case "T" -> Transitividad.TRANSITIVO;
        case "I" -> Transitividad.INTRANSITIVO;
        case "A" -> Transitividad.AMBITRANSITIVO;
        default -> throw new IllegalArgumentException(String.format("Code %s not supported", code));
        };
	}
	
	@Converter(autoApply = true)
	public static class TransitividadConverter implements AttributeConverter<Transitividad, String> {
		
		@Override
		public String convertToDatabaseColumn(Transitividad attribute) {
			return Optional.ofNullable(attribute).map(Transitividad::getCode).orElse(null);
		}

		@Override
		public Transitividad convertToEntityAttribute(String dbData) {
			return Transitividad.fromCode(dbData);
		}
	}

}
