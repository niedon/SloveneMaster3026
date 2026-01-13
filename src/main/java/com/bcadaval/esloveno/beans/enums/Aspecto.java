package com.bcadaval.esloveno.beans.enums;

import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Enum que representa los aspectos verbales en esloveno.
 * PERFECTIVO (P): Indica una acción completa o terminada.
 * IMPERFECTIVO (I): Indica una acción en progreso o habitual.
 * AMBIPREFECTIVO (*): Indica que el verbo puede usarse en ambos aspectos.
 */
public enum Aspecto {

	PERFECTIVO("P", "perfective"), IMPERFECTIVO("I", "progressive"), AMBIPREFECTIVO("*", "biaspectual");

	private String code;
	private String xmlCode;

	private Aspecto(String code, String xmlCode) {
		this.code = code;
		this.xmlCode = xmlCode;
	}

	public String getCode() {
		return code;
	}

	public String getXmlCode() {
		return xmlCode;
	}

	public static Aspecto fromCode(String code) {
		if (code == null || code.isBlank()) return null;
		return switch (code) {
		case "I", "progressive" -> Aspecto.IMPERFECTIVO;
		case "P", "perfective" -> Aspecto.PERFECTIVO;
		case "*", "biaspectual" -> Aspecto.AMBIPREFECTIVO;
		default -> null;
		};
	}
	
	@Converter(autoApply = true)
	public static class AspectoConverter implements AttributeConverter<Aspecto, String> {
		
		@Override
		public String convertToDatabaseColumn(Aspecto attribute) {
			return Optional.ofNullable(attribute).map(Aspecto::getCode).orElse(null);
		}

		@Override
		public Aspecto convertToEntityAttribute(String dbData) {
			return Aspecto.fromCode(dbData);
		}
	}

}
