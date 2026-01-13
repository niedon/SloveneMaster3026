package com.bcadaval.esloveno.structures;

import lombok.Builder;
import lombok.Data;

/**
 * Representa un dato de visualización para el JSP.
 * El JSP es transparente al modo de visualización, solo consulta textoFila1 y textoFila2.
 */
@Data
@Builder
public class DatoVisualizacion {

    /**
     * Texto a mostrar en la fila 1 (pregunta).
     * Puede ser español o esloveno según el modo, pero el JSP no necesita saberlo.
     */
    private String textoFila1;

    /**
     * Texto a mostrar en la fila 2 (respuesta).
     * Puede ser esloveno o español según el modo, pero el JSP no necesita saberlo.
     */
    private String textoFila2;

    /**
     * ID de la palabra flexionada para el SRS.
     * Puede ser null si la palabra no tiene ID (ej: pronombres, números).
     */
    private Integer id;

    /**
     * Tipo de palabra para identificar en RespuestasController.
     */
    private FraseTipoPalabra tipo;
}

