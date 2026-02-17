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

    /**
     * Texto formateado del intervalo si la respuesta es correcta (👍).
     * Ejemplo: "10 minutos", "2 horas", "1 día"
     */
    private String intervaloArriba;

    /**
     * Texto formateado del intervalo si la respuesta es incorrecta (👎).
     * Ejemplo: "30 segundos"
     */
    private String intervaloAbajo;

    /**
     * Formatea un intervalo en segundos a texto legible en español.
     *
     * @param segundos El intervalo en segundos
     * @return Texto formateado (ej: "10 min", "2 h", "1 día", "3 meses")
     */
    public static String formatearIntervalo(long segundos) {
        if (segundos < 60) {
            return segundos + " segundo/s";
        } else if (segundos < 3600) {
            return (segundos / 60) + " minuto/s";
        } else if (segundos < 86400) {
            return (segundos / 3600) + " hora/s";
        } else if (segundos < 604800) { // menos de 7 días
            return segundos / 86400 + " día/s";
        } else if (segundos < 2592000) { // menos de 30 días
            return (segundos / 604800) + " semana/s";
        } else if (segundos < 31536000) { // menos de 365 días
            return (segundos / 2592000) + " mes/es";
        } else {
            return (segundos / 31536000) + " año/s";
        }
    }
}

