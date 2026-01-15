package com.bcadaval.esloveno.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar una palabra incompleta
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PalabraIncompletaDTO {

    /**
     * Indica si el sustantivo es animado (puede ser null)
     */
    private Boolean animado;
    /**
     * Transitividad del verbo (puede ser null)
     */
    private String transitividad;
    /**
     * Significado en español (puede ser null)
     */
    private String significado;

    /**
     * Tipo de palabra (verbo, sustantivo)
     */
    private String tipo;

    /**
     * La palabra en esloveno
     */
    private String palabra;
    /**
     * ID de la palabra (principal)
     */
    private String id;

}





