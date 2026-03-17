package com.bcadaval.esloveno.beans.base;

import java.time.Instant;

/**
 * Interfaz base para las flexiones de palabras.
 * Incluye métodos para el Sistema de Repetición Espaciada (SRS).
 */
public interface PalabraFlexion<T extends Palabra<?>> {

    /**
     * Obtiene el identificador único de la flexión.
     *
     * @return El ID de la flexión.
     */
    Integer getId();

    /**
     * Obtiene el identificador Sloleks de la palabra base.
     *
     * @return El sloleksId de la palabra base.
     */
    String getSloleksId();

    String getSignificado();

    String getAcentuado();

    String getFlexion();

    void setPalabraBase(T palabra);


    // =====================================================
    // Métodos del Sistema de Repetición Espaciada (SRS)
    // =====================================================

    Double getFactorFacilidad();
    PalabraFlexion<T> setFactorFacilidad(Double factor);

    Long getIntervaloRepeticionSegundos();
    PalabraFlexion<T> setIntervaloRepeticionSegundos(Long intervalo);

    Integer getVecesConsecutivasCorrectas();
    PalabraFlexion<T> setVecesConsecutivasCorrectas(Integer veces);

    Instant getUltimaRevision();
    PalabraFlexion<T> setUltimaRevision(Instant instant);

    Instant getProximaRevision();
    PalabraFlexion<T> setProximaRevision(Instant instant);

    Integer getTotalRevisiones();
    PalabraFlexion<T> setTotalRevisiones(Integer total);

    Integer getTotalAciertos();
    PalabraFlexion<T> setTotalAciertos(Integer total);

    Boolean getEnReaprendizaje();
    PalabraFlexion<T> setEnReaprendizaje(Boolean enReaprendizaje);

    // =====================================================
    // Campo de elegibilidad
    // =====================================================

    /**
     * Indica si esta flexión es elegible para estudio.
     * Una flexión es elegible si su palabra base está completa y
     * cumple al menos un criterio de las frases activas.
     *
     * @return {@code true} si la flexión es elegible para estudio
     */
    Boolean getElegible();

    /**
     * Establece la elegibilidad de esta flexión.
     *
     * @param elegible {@code true} si la flexión es elegible
     * @return esta flexión para encadenamiento fluido
     */
    PalabraFlexion<T> setElegible(Boolean elegible);
}
