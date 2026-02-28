package com.bcadaval.esloveno.structures.frase.dependencia;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.structures.frase.criterio.CriterioBusquedaNuevo;

import java.util.function.Predicate;

/**
 * Tupla que asocia un predicado con un criterio de búsqueda
 * dentro de una {@link Dependencia}.
 * <p>
 * El predicado se evalúa contra la {@link PalabraFlexion} asignada al hueco referenciado.
 * Si devuelve {@code true}, se aplica el criterio asociado.
 *
 * @param predicado función que recibe la palabra asignada al hueco dependido y devuelve si la condición se cumple
 * @param criterio  criterio de búsqueda a aplicar si el predicado se cumple
 * @param <S>       tipo de {@link PalabraFlexion} del hueco del que se depende
 */
public record CondicionDependencia<S extends PalabraFlexion<?>>(
        Predicate<S> predicado,
        CriterioBusquedaNuevo<?> criterio
) {
}

