package com.bcadaval.esloveno.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar el detalle de una flexión con estadísticas SRS.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlexionDetalleDTO {

    /**
     * ID de la flexión
     */
    private Integer id;

    /**
     * Texto de la flexión
     */
    private String flexion;

    /**
     * Texto con acentuación
     */
    private String acentuado;

    // =====================================================
    // Propiedades gramaticales (nulls si no aplican al tipo)
    // =====================================================

    /**
     * Forma verbal (solo verbos)
     */
    private String formaVerbal;

    /**
     * Persona gramatical (solo verbos)
     */
    private String persona;

    /**
     * Número gramatical
     */
    private String numero;

    /**
     * Género
     */
    private String genero;

    /**
     * Caso gramatical
     */
    private String caso;

    /**
     * Grado (solo adjetivos)
     */
    private String grado;

    /**
     * Definitud (solo adjetivos)
     */
    private String definitud;

    /**
     * Negativo (solo verbos)
     */
    private Boolean negativo;

    // =====================================================
    // Campos SRS
    // =====================================================

    /**
     * Factor de facilidad
     */
    private Double factorFacilidad;

    /**
     * Intervalo de repetición en segundos
     */
    private Long intervaloRepeticionSegundos;

    /**
     * Veces consecutivas correctas
     */
    private Integer vecesConsecutivasCorrectas;

    /**
     * Total de revisiones realizadas
     */
    private Integer totalRevisiones;

    /**
     * Total de aciertos
     */
    private Integer totalAciertos;

    /**
     * Tasa de aciertos en porcentaje
     */
    private Double tasaAciertos;

    /**
     * Si está en reaprendizaje
     */
    private Boolean enReaprendizaje;

    /**
     * Si la flexión está activa (proximaRevision != null, es decir, SRS inicializado)
     */
    private boolean activa;

    /**
     * Si la flexión es elegible según las estructuras de frase activas actualmente
     * (cumple los criterios gramaticales de al menos una estructura activa)
     */
    private boolean elegible;

    /**
     * Si el estudio ha comenzado (totalRevisiones > 0)
     */
    private boolean estudioIniciado;

    /**
     * Próxima revisión en formato legible
     */
    private String proximaRevision;

    /**
     * Última revisión en formato legible
     */
    private String ultimaRevision;

    /**
     * Intervalo de repetición en formato legible
     */
    private String intervaloLegible;
}

