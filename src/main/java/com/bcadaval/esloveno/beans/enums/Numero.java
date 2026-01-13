package com.bcadaval.esloveno.beans.enums;

import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Número gramatical: singular, dual, plural
 */
public enum Numero {
	SINGULAR("1", "singular"), DUAL("2", "dual"), PLURAL("3", "plural");

	private String code;
	private String xmlCode;

	private Numero(String code, String xmlCode) {
		this.code = code;
		this.xmlCode = xmlCode;
	}

	public String getCode() {
		return code;
	}
	
	public String getXmlCode() {
		return xmlCode;
	}

	public static Numero fromCode(String code) {
		if (code == null || code.isBlank()) return null;
		return switch (code) {
	        case "1", "singular" -> Numero.SINGULAR;
	        case "2", "dual" -> Numero.DUAL;
	        case "3", "plural" -> Numero.PLURAL;
	        default -> null;
        };
	}

	public static Numero getRandom() {
		Numero[] values = Numero.values();
		int randomIndex = (int) (Math.random() * values.length);
		return values[randomIndex];
	}
	
	@Converter(autoApply = true)
	public static class NumeroConverter implements AttributeConverter<Numero, String> {
		
		@Override
		public String convertToDatabaseColumn(Numero attribute) {
			return Optional.ofNullable(attribute).map(Numero::getCode).orElse(null);
		}

		@Override
		public Numero convertToEntityAttribute(String dbData) {
			return Numero.fromCode(dbData);
		}
	}
}
