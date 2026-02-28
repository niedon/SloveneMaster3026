package com.bcadaval.esloveno.structures.frase.criterio;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.structures.frase.dependencia.Dependencia;

import java.util.*;

/**
 * Clase base abstracta para los builders tipados de {@link CriterioBusquedaNuevo}.
 * <p>
 * Gestiona el estado interno del builder: las restricciones (mapa campo → valores)
 * y las dependencias. Los builders concretos heredan de esta clase y exponen
 * únicamente los métodos gramaticales relevantes para su tipo de flexión.
 * <p>
 * Cada método de restricción acepta varargs, lo que significa "OR entre los valores dados".
 * Múltiples llamadas a métodos distintos se combinan con AND.
 *
 * @param <T> Tipo concreto de {@link PalabraFlexion}
 * @param <B> Tipo del builder concreto (para retorno fluido)
 */
public abstract class CriterioBuilderBase<T extends PalabraFlexion<?>, B extends CriterioBuilderBase<T, B>> {

    /**
     * Restricciones acumuladas: nombre de campo → conjunto de valores aceptados.
     */
    protected final Map<String, Set<Object>> restricciones = new LinkedHashMap<>();

    /**
     * Dependencias condicionales acumuladas.
     */
    protected final List<Dependencia<?>> dependencias = new ArrayList<>();

    /**
     * Devuelve la clase del tipo de flexión. Cada builder concreto la proporciona.
     *
     * @return clase del tipo de flexión (ej. {@code SustantivoFlexion.class})
     */
    protected abstract Class<T> getTipoFlexion();

    /**
     * Retorna {@code this} con el tipo del builder concreto para encadenamiento fluido.
     *
     * @return referencia a este builder con tipo concreto
     */
    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }

    /**
     * Añade una restricción sobre un campo de la flexión.
     * Los valores proporcionados se interpretan como OR entre ellos.
     * <p>
     * Si se llama múltiples veces con el mismo campo, se acumulan los valores (OR).
     *
     * @param campo   nombre del campo en la entidad JPA
     * @param valores valores aceptados (varargs, OR entre ellos)
     * @return este builder para encadenamiento fluido
     */
    protected B agregarRestriccion(String campo, Object... valores) {
        restricciones.computeIfAbsent(campo, k -> new LinkedHashSet<>())
                .addAll(Arrays.asList(valores));
        return self();
    }

    /**
     * Añade una dependencia condicional de otro hueco ({@link com.bcadaval.esloveno.structures.frase.PalabraFrase}).
     * <p>
     * En tiempo de asignación ({@code /getWords}), la dependencia se evalúa contra
     * la palabra ya asignada al hueco referenciado. En tiempo de cálculo de palabras
     * estudiables, se expanden todas las ramas posibles de la dependencia.
     *
     * @param dependencia dependencia construida con {@link com.bcadaval.esloveno.structures.frase.dependencia.DependenciaBuilder}
     * @return este builder para encadenamiento fluido
     */
    public B conDependencia(Dependencia<?> dependencia) {
        this.dependencias.add(dependencia);
        return self();
    }

    /**
     * Construye el {@link CriterioBusquedaNuevo} con las restricciones y dependencias acumuladas.
     *
     * @return criterio de búsqueda inmutable listo para usar
     */
    public CriterioBusquedaNuevo<T> build() {
        return new CriterioBusquedaNuevo<>(getTipoFlexion(), new LinkedHashMap<>(restricciones), new ArrayList<>(dependencias));
    }
}

