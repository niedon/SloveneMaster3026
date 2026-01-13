package com.bcadaval.esloveno.structures;

/**
 * Modo de visualización de la frase.
 * Define qué idioma se muestra en cada fila de la tabla.
 */
public enum ModoVisualizacion {

    /**
     * Español a Esloveno:
     * - Fila 1 (pregunta): texto en español
     * - Fila 2 (respuesta): texto en esloveno (con acentuación)
     */
    ES_SL,

    /**
     * Esloveno a Español:
     * - Fila 1 (pregunta): texto en esloveno (sin acentuación)
     * - Fila 2 (respuesta): texto en español
     */
    SL_ES;

    /**
     * Obtiene un modo aleatorio
     */
    public static ModoVisualizacion aleatorio() {
        return Math.random() < 0.5 ? ES_SL : SL_ES;
    }
}

