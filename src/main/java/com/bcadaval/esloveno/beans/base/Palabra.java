package com.bcadaval.esloveno.beans.base;

import java.util.List;

/**
 * Representa una palabra genérica en esloveno. La palabra tendrá varias formas que dependan de las categorías
 * gramaticales como Aspecto, Caso, Género, Grado, Número, Persona o Transitividad.
 *
 * @param <E> Clase que representa la flexión de la palabra.
 */
public interface Palabra<E> {

    /**
     * Obtiene la lista de flexiones asociadas a la palabra.
     *
     * @return Lista de flexiones de la palabra.
     */
    List<E> getListaFlexiones();

    /**
     * Obtiene la forma principal de la palabra.
     *
     * @return La forma principal de la palabra.
     */
    String getPrincipal();

}
