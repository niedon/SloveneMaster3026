package com.bcadaval.esloveno.beans.enums;

import lombok.Getter;

@Getter
public enum CategoriaFrase {
    // ================================= NIVEL 1 =================================
    SUSTANTIVOS(10, "Sustantivos", "Frases introductorias con sustantivos", NivelDificultad.NIVEL_1),
    VERBOS(11, "Verbos", "Frases introductorias con verbos", NivelDificultad.NIVEL_1),
    PRESENTE(12, "Presente", "Uso del tiempo presente estructural", NivelDificultad.NIVEL_1),

    // ================================= NIVEL 2 =================================
    PRESENTE_NEGADO(20, "Presente negado", "Estructuras negadas en presente", NivelDificultad.NIVEL_2),
    PRESENTE_CON_CD(21, "Presente con CD", "Presente con complemento directo", NivelDificultad.NIVEL_2),

    // ================================= NIVEL 3 =================================
    CANTIDADES_MAYOR_5(30, "Cantidades > 5", "Uso de números mayores a 5 con genitivo", NivelDificultad.NIVEL_3),
    NEGACION_PRESENTE_CON_CD(31, "Presente negado con CD", "Negación con complemento directo (genitivo)", NivelDificultad.NIVEL_3),
    RELACION_NOMINAL(32, "Relación nominal (X de Y)", "Relación de pertenencia o característica", NivelDificultad.NIVEL_3),

    // ================================= NIVEL 4 =================================
    PASADO_SIMPLE(40, "Pasado simple", "Estructuras simples en pasado", NivelDificultad.NIVEL_4),
    PASADO_CON_CD(41, "Pasado con CD", "Pasado con complemento directo", NivelDificultad.NIVEL_4),

    // ================================= NIVEL 5 =================================
    FUTURO_SIMPLE(50, "Futuro simple", "Estructuras simples en futuro", NivelDificultad.NIVEL_5),
    FUTURO_CON_CD(51, "Futuro con CD", "Futuro con complemento directo", NivelDificultad.NIVEL_5),


    // ================================= NIVEL 6 =================================
    EN_PROCESO(99, "En proceso", "Otras frases en proceso", NivelDificultad.NIVEL_6);

    private final int orden;
    private final String titulo;
    private final String descripcion;
    private final NivelDificultad nivelDificultad;

    CategoriaFrase(int orden, String titulo, String descripcion, NivelDificultad nivelDificultad) {
        this.orden = orden;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.nivelDificultad = nivelDificultad;
    }
}
