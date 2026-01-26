package com.bcadaval.esloveno.beans.enums;

/**
 * Enum unificado de todas las características gramaticales posibles.
 * Se usa para filtrado en memoria de PalabraFlexion.
 * <p>
 * IMPORTANTE: Al añadir un nuevo tipo de PalabraFlexion, revisar que
 * implemente correctamente getCaracteristica() para todas las características
 * que le apliquen.
 */
public enum CaracteristicaGramatical {

    // Características comunes
    NUMERO,
    GENERO,

    // Características de sustantivos/adjetivos/pronombres
    CASO,

    // Características de adjetivos
    GRADO,
    DEFINITUD,

    // Características de verbos
    FORMA_VERBAL,
    PERSONA,
    TRANSITIVIDAD,
    NEGATIVO,

    // Características de numerales
    TIPO_NUMERAL
}
