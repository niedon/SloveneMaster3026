package com.bcadaval.esloveno.structures.frase.dependencia;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.structures.frase.PalabraFrase;
import com.bcadaval.esloveno.structures.frase.criterio.CriterioBusquedaNuevo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Builder fluido para construir {@link Dependencia dependencias} condicionales entre huecos.
 * <p>
 * Permite definir criterios condicionales que se aplican según el valor
 * de la {@link PalabraFlexion} asignada a otro hueco ({@link PalabraFrase}).
 * <p>
 * <strong>Contrato de uso:</strong>
 * <ol>
 *   <li>{@code DependenciaBuilder.de(otraPalabraFrase)} — establece el hueco del que se depende</li>
 *   <li>{@code .si(predicado, criterio)} — añade una condición (puede haber varias, evaluadas en orden)</li>
 *   <li>{@code .orElse(criterio)} — establece el fallback obligatorio y devuelve la {@link Dependencia}</li>
 * </ol>
 * <p>
 * El compilador fuerza que {@code .orElse()} se llame antes de poder usar la dependencia,
 * ya que {@code .si()} devuelve el builder (sin función para obtener la dependencia),
 * y solo {@code .orElse()} devuelve la {@link Dependencia} construida.
 * <p>
 * <strong>Ejemplo:</strong>
 * <pre>
 * Dependencia&lt;SustantivoFlexion&gt; dep = DependenciaBuilder.de(sustantivo)
 *     .si(sust -&gt; sust.getNumero() == Numero.SINGULAR,
 *         CriterioBusquedaNuevo.de(AdjetivoFlexion.class).conCaso(Caso.NOMINATIVO).build())
 *     .si(sust -&gt; sust.getNumero() == Numero.DUAL,
 *         CriterioBusquedaNuevo.de(AdjetivoFlexion.class).conCaso(Caso.GENITIVO).build())
 *     .orElse(
 *         CriterioBusquedaNuevo.de(AdjetivoFlexion.class).conCaso(Caso.GENITIVO, Caso.INSTRUMENTAL).build());
 * </pre>
 * <p>
 * <strong>Semántica de evaluación:</strong>
 * <ul>
 *   <li>En tiempo de asignación: se evalúan los {@code .si()} en orden contra la palabra
 *       asignada al hueco referenciado. El primero que devuelve {@code true} aporta su criterio.
 *       Si ninguno se cumple, se aplica el criterio del {@code .orElse()}.</li>
 *   <li>En tiempo de cálculo de palabras estudiables: se toman <strong>todos</strong> los criterios
 *       (de todos los {@code .si()} y del {@code .orElse()}) como alternativas posibles.</li>
 * </ul>
 *
 * @param <S> Tipo de {@link PalabraFlexion} del hueco del que se depende
 */
public class DependenciaBuilder<S extends PalabraFlexion<?>> {

    private final PalabraFrase<S> huecoReferenciado;
    private final List<CondicionDependencia<S>> condiciones = new ArrayList<>();

    private DependenciaBuilder(PalabraFrase<S> huecoReferenciado) {
        this.huecoReferenciado = huecoReferenciado;
    }

    /**
     * Punto de entrada: establece el hueco ({@link PalabraFrase}) del que depende esta condición.
     * El tipo genérico {@code S} se infiere del tipo del hueco, lo que permite que
     * los predicados de {@code .si()} reciban directamente el tipo concreto de la flexión.
     *
     * @param huecoReferenciado hueco del que depende
     * @param <S>               tipo de flexión del hueco referenciado
     * @return builder listo para añadir condiciones
     */
    public static <S extends PalabraFlexion<?>> DependenciaBuilder<S> de(PalabraFrase<S> huecoReferenciado) {
        return new DependenciaBuilder<>(huecoReferenciado);
    }

    /**
     * Añade una condición evaluada en orden (tipo if/else if).
     * <p>
     * El predicado recibe directamente el tipo concreto de la flexión del hueco referenciado.
     * Por ejemplo, si el hueco es {@code PalabraFrase<SustantivoFlexion>}, el predicado
     * recibe un {@code SustantivoFlexion}.
     *
     * @param predicado función que evalúa la palabra asignada al hueco referenciado
     * @param criterio  criterio a aplicar si el predicado devuelve {@code true}
     * @return este builder para encadenamiento
     */
    public DependenciaBuilder<S> si(Predicate<S> predicado, CriterioBusquedaNuevo<?> criterio) {
        condiciones.add(new CondicionDependencia<>(predicado, criterio));
        return this;
    }

    /**
     * Establece el criterio fallback obligatorio y construye la {@link Dependencia}.
     * <p>
     * Esta función es la única que devuelve la dependencia construida,
     * forzando al compilador a exigir su invocación antes de poder usar la dependencia.
     *
     * @param criterioDefault criterio a aplicar si ninguna condición {@code .si()} se cumple
     * @return la dependencia construida e inmutable
     * @throws IllegalStateException si no se ha añadido al menos una condición {@code .si()}
     */
    public Dependencia<S> orElse(CriterioBusquedaNuevo<?> criterioDefault) {
        if (condiciones.isEmpty()) {
            throw new IllegalStateException(
                    "DependenciaBuilder requiere al menos una condición .si() antes de .orElse()");
        }
        return new Dependencia<>(huecoReferenciado, condiciones, criterioDefault);
    }
}

