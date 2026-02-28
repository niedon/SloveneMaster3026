package com.bcadaval.esloveno.structures.frase.dependencia;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.CriterioBusquedaNuevo;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una dependencia condicional de un hueco ({@link PalabraFrase}) respecto a otro.
 * <p>
 * Contiene una serie de condiciones ({@code si}) evaluadas en orden y un fallback ({@code orElse}).
 * Cuando el hueco referenciado tiene una {@link PalabraFlexion} asignada, se evalúan las condiciones
 * en orden: la primera que devuelve {@code true} aporta su {@link CriterioBusquedaNuevo}.
 * Si ninguna condición se cumple, se aplica el criterio del {@code orElse}.
 * <p>
 * <strong>En tiempo de asignación ({@code /getWords}):</strong> se resuelve la dependencia
 * evaluando los predicados contra la palabra asignada al hueco padre.
 * <p>
 * <strong>En tiempo de cálculo de palabras estudiables:</strong> se obtienen todas las ramas
 * posibles (todos los criterios de los {@code si} + el {@code orElse}) para generar el producto
 * cartesiano con las demás dependencias.
 *
 * @param <S> Tipo de {@link PalabraFlexion} del hueco del que se depende
 */
public class Dependencia<S extends PalabraFlexion<?>> {

    /**
     * Hueco del que depende este criterio.
     * -- GETTER --
     *  Obtiene el hueco del que depende esta dependencia.
     */
    @Getter
    private final PalabraFrase<S> huecoReferenciado;

    /**
     * Lista ordenada de condiciones (evaluadas en orden, tipo if/else if).
     */
    private final List<CondicionDependencia<S>> condiciones;

    /**
     * Criterio fallback obligatorio, aplicado si ninguna condición se cumple.
     */
    private final CriterioBusquedaNuevo<?> criterioDefault;

    Dependencia(PalabraFrase<S> huecoReferenciado,
                List<CondicionDependencia<S>> condiciones,
                CriterioBusquedaNuevo<?> criterioDefault) {
        this.huecoReferenciado = huecoReferenciado;
        this.condiciones = List.copyOf(condiciones);
        this.criterioDefault = criterioDefault;
    }

    /**
     * Indica si el hueco referenciado ya tiene una palabra asignada,
     * lo que permite evaluar la dependencia.
     *
     * @return {@code true} si el hueco referenciado tiene palabra asignada
     */
    public boolean estaResuelta() {
        return huecoReferenciado.estaAsignado();
    }

    /**
     * Resuelve la dependencia evaluando las condiciones contra la palabra
     * asignada al hueco referenciado.
     * <p>
     * Se evalúan las condiciones en orden; la primera que devuelve {@code true}
     * aporta su criterio. Si ninguna se cumple, se aplica el criterio default ({@code orElse}).
     *
     * @return criterio resultante de la evaluación, o {@code null} si la dependencia no está resuelta
     */
    public CriterioBusquedaNuevo<?> resolver() {
        if (!estaResuelta()) return null;

        S palabraAsignada = huecoReferenciado.getPalabraAsignada();

        for (CondicionDependencia<S> condicion : condiciones) {
            if (condicion.predicado().test(palabraAsignada)) {
                return condicion.criterio();
            }
        }

        return criterioDefault;
    }

    /**
     * Obtiene todos los criterios posibles de esta dependencia (todas las ramas).
     * Se usa para el cálculo de palabras estudiables donde no se conoce
     * qué condición se activará.
     * <p>
     * Devuelve los criterios de todos los {@code .si()} + el {@code .orElse()}.
     *
     * @return lista de todos los criterios posibles (nunca vacía)
     */
    public List<CriterioBusquedaNuevo<?>> obtenerTodasLasRamas() {
        List<CriterioBusquedaNuevo<?>> ramas = new ArrayList<>();
        for (CondicionDependencia<S> condicion : condiciones) {
            ramas.add(condicion.criterio());
        }
        ramas.add(criterioDefault);
        return ramas;
    }

}
