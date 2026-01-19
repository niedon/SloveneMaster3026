package com.bcadaval.esloveno.beans.base;

import java.time.Instant;

import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;

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

    /**
     * Obtiene el valor de una característica gramatical.
     * Cada implementación debe hacer switch exhaustivo sobre CaracteristicaGramatical.
     *
     * @param caracteristica La característica a obtener
     * @return El valor de la característica, o null si no aplica a este tipo
     */
    default Object getCaracteristica(CaracteristicaGramatical caracteristica) {
        return null;
    }

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
