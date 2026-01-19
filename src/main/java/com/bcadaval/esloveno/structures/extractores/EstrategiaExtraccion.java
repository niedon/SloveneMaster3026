package com.bcadaval.esloveno.structures.extractores;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;

import java.util.function.Function;

/**
 * Interfaz que define los 4 extractores necesarios para obtener el texto
 * de una PalabraFlexion según el modo de visualización.
 * <p>
 * Implementaciones singleton reutilizables permiten evitar repetición
 * de extractores comunes en las estructuras de frase.
 */
public interface EstrategiaExtraccion<T extends PalabraFlexion<?>> {

    /**
     * Extractor para modo ES_SL - Fila 1 (pregunta en español)
     * Ejemplo típico: getSignificado()
     */
    Function<T, String> deEspanol();

    /**
     * Extractor para modo ES_SL - Fila 2 (respuesta en esloveno)
     * Ejemplo típico: getAcentuado() o getFlexion()
     */
    Function<T, String> aEsloveno();

    /**
     * Extractor para modo SL_ES - Fila 1 (pregunta en esloveno)
     * Ejemplo típico: getFlexion()
     */
    Function<T, String> deEsloveno();

    /**
     * Extractor para modo SL_ES - Fila 2 (respuesta en español)
     * Ejemplo típico: getSignificado()
     */
    Function<T, String> aEspanol();

}

