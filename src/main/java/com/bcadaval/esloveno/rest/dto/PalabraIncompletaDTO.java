package com.bcadaval.esloveno.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar una palabra (incompleta o completa) en la pantalla de completar/editar palabras
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PalabraIncompletaDTO {

    // =====================================================
    // Campos comunes
    // =====================================================

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

    /**
     * Indica si la palabra está completa (todos los campos obligatorios rellenos)
     */
    private boolean completa;

    // =====================================================
    // Campos específicos de sustantivo
    // =====================================================

    /**
     * Animacidad del sustantivo (ANIMADO/INANIMADO, puede ser null)
     */
    private String animacidad;

    /**
     * Contabilidad del sustantivo (CONTABLE/INCONTABLE, puede ser null)
     */
    private String contabilidad;

    /**
     * Clase semántica del sustantivo (HUMANO/ANIMAL/OBJETO/LUGAR/SUSTANCIA/ABSTRACTO, puede ser null)
     */
    private String claseSemantica;

    /**
     * Cabeza relacional del sustantivo (SI/NO, puede ser null)
     */
    private String cabezaRelacional;

    // =====================================================
    // Campos específicos de verbo
    // =====================================================

    /**
     * Transitividad del verbo (puede ser null)
     */
    private String transitividad;

    /**
     * Indica si el verbo requiere sujeto animado (SI/NO, puede ser null)
     */
    private String requiereSujetoAnimado;

    /**
     * Indica si el verbo requiere objeto animado (SI/NO, puede ser null)
     */
    private String requiereObjetoAnimado;

    // =====================================================
    // Campos específicos de numeral
    // =====================================================

    /**
     * Representación numérica del numeral (puede ser null)
     */
    private Integer cantidad;

}





