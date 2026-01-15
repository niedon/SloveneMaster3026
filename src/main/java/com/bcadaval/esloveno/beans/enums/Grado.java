package com.bcadaval.esloveno.beans.enums;

import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Getter;

/**
 * Grado de un adjetivo: positivo, comparativo, superlativo
 */
@Getter
public enum Grado {

	POSITIVO("P", "positive"), COMPARATIVO("C", "comparative"), SUPERLATIVO("S", "superlative");

	private final String code;
	private final String xmlCode;

	Grado(String code, String xmlCode) {
		this.code = code;
		this.xmlCode = xmlCode;
	}

    public static Grado fromCode(String code) {
		if (code == null || code.isBlank()) return null;
		return switch (code) {
		case "P", "positive" -> Grado.POSITIVO;
		case "C", "comparative" -> Grado.COMPARATIVO;
		case "S", "superlative" -> Grado.SUPERLATIVO;
		default -> null;
		};
	}
	
	@Converter(autoApply = true)
	public static class GradoConverter implements AttributeConverter<Grado, String> {

		@Override
		public String convertToDatabaseColumn(Grado attribute) {
			return Optional.ofNullable(attribute).map(Grado::getCode).orElse(null);
		}

		@Override
		public Grado convertToEntityAttribute(String dbData) {
			return Grado.fromCode(dbData);
		}
	}

}
