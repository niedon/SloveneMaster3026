package com.bcadaval.esloveno.beans.enums;

import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persona gramatical: primera, segunda, tercera
 */
public enum Persona {
	PRIMERA("1", "first"), SEGUNDA("2", "second"), TERCERA("3", "third");

	private String code;
	private String xmlCode;

	private Persona(String code, String xmlCode) {
		this.code = code;
		this.xmlCode = xmlCode;
	}

	public String getCode() {
		return code;
	}

	public String getXmlCode() {
		return xmlCode;
	}

	public static Persona fromCode(String code) {
		if (code == null || code.isBlank()) return null;
		return switch (code) {
		case "1", "first", "S" -> Persona.PRIMERA;
		case "2", "second", "D" -> Persona.SEGUNDA;
		case "3", "third", "P" -> Persona.TERCERA;
		default -> null;
		};
	}

	@Converter(autoApply = true)
	public static class PersonaConverter implements AttributeConverter<Persona, String> {
		
		@Override
		public String convertToDatabaseColumn(Persona attribute) {
			return Optional.ofNullable(attribute).map(Persona::getCode).orElse(null);
		}

		@Override
		public Persona convertToEntityAttribute(String dbData) {
			return Persona.fromCode(dbData);
		}
	}
}
