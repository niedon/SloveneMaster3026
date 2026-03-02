package com.bcadaval.esloveno.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar una palabra guardada en el sistema.
 * Incluye propiedades comunes y tipo-específicas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PalabraGuardadaDTO {

    /**
     * ID de la palabra (sloleksId)
     */
    private String sloleksId;

    /**
     * Forma principal de la palabra en esloveno
     */
    private String principal;

    /**
     * Significado en español
     */
    private String significado;

    /**
     * Tipo de palabra (xmlCode: noun, verb, adjective, pronoun, numeral)
     */
    private String tipo;

    /**
     * Nombre del tipo en español (Verbo, Sustantivo, etc.)
     */
    private String tipoEspanol;

    /**
     * Indica si la palabra está completa (significado asignado + campos obligatorios según tipo)
     */
    private boolean completa;

    // =====================================================
    // Propiedades tipo-específicas (nulls si no aplican)
    // =====================================================

    /**
     * Transitividad del verbo (TRANSITIVO, INTRANSITIVO, AMBITRANSITIVO)
     */
    private String transitividad;

    /**
     * Aspecto del verbo (PERFECTIVO, IMPERFECTIVO, AMBIPREFECTIVO)
     */
    private String aspecto;

    /**
     * Verbo del otro aspecto (si existe)
     */
    private String verboOtroAspecto;

    /**
     * Género del sustantivo (MASCULINO, FEMENINO, NEUTRO)
     */
    private String genero;

    /**
     * Animacidad del sustantivo (ANIMADO, INANIMADO)
     */
    private String animacidad;

    /**
     * Contabilidad del sustantivo (CONTABLE, INCONTABLE)
     */
    private String contabilidad;

    /**
     * Clase semántica del sustantivo (HUMANO, ANIMAL, OBJETO, LUGAR, SUSTANCIA, ABSTRACTO)
     */
    private String claseSemantica;

    /**
     * Cabeza relacional del sustantivo (SI, NO)
     */
    private String cabezaRelacional;

    /**
     * Si el verbo requiere sujeto animado (SI, NO)
     */
    private String requiereSujetoAnimado;

    /**
     * Si el verbo requiere objeto animado (SI, NO)
     */
    private String requiereObjetoAnimado;

    /**
     * Tipo de pronombre (PERSONAL, POSESIVO, etc.)
     */
    private String tipoPronombre;

    /**
     * Número total de flexiones de esta palabra
     */
    private int totalFlexiones;

    /**
     * Número de flexiones activas (con proximaRevision != null)
     */
    private int flexionesActivas;

    /**
     * Número de flexiones elegibles según las estructuras de frase activas
     */
    private int flexionesElegibles;

    /**
     * Indica si la palabra tiene al menos una flexión elegible
     * según las estructuras de frase activas actualmente
     */
    private boolean disponible;
}

