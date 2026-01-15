package com.bcadaval.esloveno.beans.enums;

import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Getter;

/**
 * Enum que representa los casos gramaticales en esloveno.
 * NOMINATIVO: Sujeto de la oración.
 * GENITIVO: Indica posesión o relación.
 * DATIVO: Indica el objeto indirecto.
 * ACUSATIVO: Indica el objeto directo.
 * LOCATIVO: Indica ubicación o lugar.
 * INSTRUMENTAL: Indica el medio o instrumento con el que se realiza una acción
 */
@Getter
public enum Caso{

	NOMINATIVO("1", "nominative"), GENITIVO("2", "genitive"), DATIVO("3", "dative"), ACUSATIVO("4", "accusative"),
	LOCATIVO("5", "locative"), INSTRUMENTAL("6", "instrumental");

	private final String code;
	private final String xmlCode;

	Caso(String code, String xmlCode) {
		this.code = code;
		this.xmlCode = xmlCode;
	}

    public static Caso fromCode(String code) {
		if (code == null || code.isBlank()) return null;
		return switch (code) {
		case "1", "nominative" -> Caso.NOMINATIVO;
		case "2", "genitive" -> Caso.GENITIVO;
		case "3", "dative" -> Caso.DATIVO;
		case "4", "accusative" -> Caso.ACUSATIVO;
		case "5", "locative" -> Caso.LOCATIVO;
		case "6", "instrumental" -> Caso.INSTRUMENTAL;
		default -> null;
		};
	}
	
	@Converter(autoApply = true)
	public static class CasoConverter implements AttributeConverter<Caso, String> {
		
		@Override
		public String convertToDatabaseColumn(Caso attribute) {
			return Optional.ofNullable(attribute).map(Caso::getCode).orElse(null);
		}

		@Override
		public Caso convertToEntityAttribute(String dbData) {
			return Caso.fromCode(dbData);
		}
	}

}
