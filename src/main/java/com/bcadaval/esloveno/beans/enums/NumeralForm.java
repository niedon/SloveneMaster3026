package com.bcadaval.esloveno.beans.enums;

public enum NumeralForm {

    LETTER("letter"), DIGIT("digit"), ROMAN("roman");

    private String code;

    private NumeralForm(String code) {
        this.code = code;
    }

    public static NumeralForm fromCode(String code) {
        return switch (code) {
            case "letter" -> LETTER;
            case "digit" -> DIGIT;
            case "roman" -> ROMAN;
            default -> throw new IllegalArgumentException(String.format("Code %s not supported", code));
        };
    }
}
