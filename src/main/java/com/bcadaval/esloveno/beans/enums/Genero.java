package com.bcadaval.esloveno.beans.enums;

import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Getter;

/**
 * Género gramatical: masculino, femenino, neutro
 */
@Getter
public enum Genero {
	
	MASCULINO("M", "masculine", "♂️"),
	FEMENINO("F", "feminine", "♀️"),
	NEUTRO("N", "neuter", "\uD83D\uDC64");
	
	private final String code;
	private final String xmlCode;
	private final String emoji;
	
	Genero(String code, String xmlCode, String emoji) {
		this.code = code;
		this.xmlCode = xmlCode;
		this.emoji = emoji;
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
