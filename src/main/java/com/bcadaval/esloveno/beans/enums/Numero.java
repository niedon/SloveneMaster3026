package com.bcadaval.esloveno.beans.enums;

import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Getter;

/**
 * Número gramatical: singular, dual, plural
 */
@Getter
public enum Numero {
	SINGULAR("1", "singular"), DUAL("2", "dual"), PLURAL("3", "plural");

	private final String code;
	private final String xmlCode;

	Numero(String code, String xmlCode) {
		this.code = code;
		this.xmlCode = xmlCode;
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
