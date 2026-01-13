package com.bcadaval.esloveno.beans.enums;

public enum TipoPronombre {

    INDEFINIDO("indefinite"), PERSONAL("personal"), POSESIVO("possessive"), DEMOSTRATIVO("demonstrative"), RELATIVO("relative"), INTERROGATIVO("interrogative"), REFLEXIVO("reflexive"), GENERALIZADOR("general"), NEGATIVO("negative");

    private String code;

    private TipoPronombre(String code) {
        this.code = code;
    }

    public static TipoPronombre fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return switch (code) {
            case "indefinite" -> INDEFINIDO;
            case "personal" -> PERSONAL;
            case "possessive" -> POSESIVO;
            case "demonstrative" -> DEMOSTRATIVO;
            case "relative" -> RELATIVO;
            case "interrogative" -> INTERROGATIVO;
            case "reflexive" -> REFLEXIVO;
            case "general" -> GENERALIZADOR;
            case "negative" -> NEGATIVO;
            default -> null;
        };
    }
}
