package com.bcadaval.esloveno.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de actualización de palabras
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarPalabraResponse {

    /**
     * Indica si la operación fue exitosa
     */
    private boolean exito;

    /**
     * Mensaje descriptivo del resultado
     */
    private String mensaje;

    /**
     * La palabra actualizada
     */
    private String palabra;
}

