package com.bcadaval.esloveno.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de búsqueda de palabras
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusquedaPalabraResponse {

    /**
     * Indica si la operación fue exitosa
     */
    private boolean exito;

    /**
     * Mensaje descriptivo del resultado
     */
    private String mensaje;

    /**
     * La palabra buscada
     */
    private String palabra;

    /**
     * Tipo de palabra encontrada (Verbo, Sustantivo, Adjetivo, etc.)
     */
    private String tipoPalabra;

    /**
     * Número de flexiones guardadas (opcional)
     */
    private Integer flexionesGuardadas;
}

