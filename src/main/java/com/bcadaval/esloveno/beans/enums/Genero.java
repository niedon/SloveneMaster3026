package com.bcadaval.esloveno.beans.enums;

import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Género gramatical: masculino, femenino, neutro
 */
public enum Genero {
	
	MASCULINO("M", "masculine"), FEMENINO("F", "feminine"), NEUTRO("N", "neuter");
	
	private String code;
	private String xmlCode;
	
	private Genero(String code, String xmlCode) {
		this.code = code;
		this.xmlCode = xmlCode;
	}

	public String getCode() {
		return code;
	}
	
	public String getXmlCode() {
		return xmlCode;
	}
	
	public static Genero fromCode(String code) {
		if (code == null || code.isBlank()) return null;
		return switch (code) {
        case "M", "masculine" -> Genero.MASCULINO;
        case "F", "feminine" -> Genero.FEMENINO;
        case "N", "neuter" -> Genero.NEUTRO;
        default -> null;
        };
	}

	@Converter(autoApply = true)
	public static class GeneroConverter implements AttributeConverter<Genero, String> {
		
		@Override
		public String convertToDatabaseColumn(Genero attribute) {
			return Optional.ofNullable(attribute).map(Genero::getCode).orElse(null);
		}

		@Override
		public Genero convertToEntityAttribute(String dbData) {
			return Genero.fromCode(dbData);
		}
	}
}
