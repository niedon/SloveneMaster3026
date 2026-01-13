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
}
