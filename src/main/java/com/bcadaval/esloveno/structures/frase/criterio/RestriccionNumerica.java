package com.bcadaval.esloveno.structures.frase.criterio;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Restricción sobre un campo numérico de tipo {@link Integer}.
 * <p>
 * Soporta distintos modos de comparación:
 * <ul>
 *   <li><b>VALORES</b>: el valor debe estar en un conjunto explícito de valores (OR)</li>
 *   <li><b>ENTRE</b>: el valor debe estar entre min y max, ambos inclusivos</li>
 *   <li><b>MAYOR_QUE</b>: el valor debe ser estrictamente mayor que el umbral</li>
 *   <li><b>MENOR_QUE</b>: el valor debe ser estrictamente menor que el umbral</li>
 * </ul>
 * <p>
 * Esta clase es inmutable y se usa internamente por los builders de criterio.
 * Para el cálculo de palabras estudiables, cada restricción numérica puede
 * expandirse a un conjunto de valores discretos cuando sea posible.
 */
@SuppressWarnings("unused")
public sealed interface RestriccionNumerica {

    /**
     * Evalúa si un valor cumple esta restricción.
     *
     * @param valor el valor a evaluar (puede ser null)
     * @return {@code true} si el valor cumple la restricción
     */
    boolean cumple(Integer valor);

    /**
     * Obtiene el nombre del campo al que se aplica esta restricción.
     *
     * @return nombre del campo
     */
    String campo();

    /**
     * Expande esta restricción a un conjunto de valores discretos para la expansión
     * de criterios en la consulta de palabras estudiables.
     * <p>
     * Para rangos acotados, devuelve todos los enteros en el rango.
     * Para restricciones no acotadas (mayor/menor que), devuelve el conjunto tal cual
     * ya que deberá traducirse a una condición SQL con operador.
     *
     * @return conjunto de valores discretos, o vacío si no es expandible
     */
    Set<Integer> expandirValores();

    // ============================================
    // Implementaciones
    // ============================================

    /**
     * Restricción por conjunto de valores explícitos (OR entre ellos).
     */
    record Valores(String campo, Set<Integer> valores) implements RestriccionNumerica {
        public Valores {
            valores = Collections.unmodifiableSet(new LinkedHashSet<>(valores));
        }

        @Override
        public boolean cumple(Integer valor) {
            return valor != null && valores.contains(valor);
        }

        @Override
        public Set<Integer> expandirValores() {
            return valores;
        }
    }

    /**
     * Restricción por rango inclusivo [min, max].
     */
    record Entre(String campo, int min, int max) implements RestriccionNumerica {
        @Override
        public boolean cumple(Integer valor) {
            return valor != null && valor >= min && valor <= max;
        }

        @Override
        public Set<Integer> expandirValores() {
            return IntStream.rangeClosed(min, max)
                    .boxed()
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    /**
     * Restricción por valor estrictamente mayor que un umbral.
     */
    record MayorQue(String campo, int umbral) implements RestriccionNumerica {
        @Override
        public boolean cumple(Integer valor) {
            return valor != null && valor > umbral;
        }

        @Override
        public Set<Integer> expandirValores() {
            // No se puede expandir a valores discretos, se marca como vacío
            // La consulta SQL usará un operador >
            return Collections.emptySet();
        }
    }

    /**
     * Restricción por valor estrictamente menor que un umbral.
     */
    record MenorQue(String campo, int umbral) implements RestriccionNumerica {
        @Override
        public boolean cumple(Integer valor) {
            return valor != null && valor < umbral;
        }

        @Override
        public Set<Integer> expandirValores() {
            // No se puede expandir a valores discretos, se marca como vacío
            // La consulta SQL usará un operador <
            return Collections.emptySet();
        }
    }
}


