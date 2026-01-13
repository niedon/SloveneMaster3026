package com.bcadaval.esloveno.beans.enums;

public enum NumeralType {

    SPECIAL("special"), CARDINAL("cardinal"), ORDINAL("ordinal"), PRONOMINAL("pronominal");

    private String code;

    private NumeralType(String code) {
        this.code = code;
    }

    public static NumeralType fromCode(String code) {
        return switch (code) {
            case "special" -> SPECIAL;
            case "cardinal" -> CARDINAL;
            case "ordinal" -> ORDINAL;
            case "pronominal" -> PRONOMINAL;
            default -> throw new IllegalArgumentException(String.format("Code %s not supported", code));
        };
    }
}
