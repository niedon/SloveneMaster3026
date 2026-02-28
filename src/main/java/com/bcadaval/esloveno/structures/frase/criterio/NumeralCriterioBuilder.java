package com.bcadaval.esloveno.structures.frase.criterio;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;

/**
 * Builder tipado de {@link CriterioBusquedaNuevo} para {@link NumeralFlexion}.
 * <p>
 * Expone las características gramaticales relevantes para numerales:
 * <ul>
 *   <li>{@link #conGenero(Genero...)} — Género (campo de la flexión)</li>
 *   <li>{@link #conNumero(Numero...)} — Número gramatical (campo de la flexión)</li>
 *   <li>{@link #conCaso(Caso...)} — Caso gramatical (campo de la flexión)</li>
 * </ul>
 * <p>
 * Ejemplo de uso:
 * <pre>
 * CriterioBusquedaNuevo.de(NumeralFlexion.class)
 *     .conCaso(Caso.NOMINATIVO)
 *     .conGenero(Genero.MASCULINO)
 *     .build();
 * </pre>
 */
public class NumeralCriterioBuilder extends CriterioBuilderBase<NumeralFlexion, NumeralCriterioBuilder> {

    /**
     * Factory method estático.
     */
    public static NumeralCriterioBuilder crear() {
        return new NumeralCriterioBuilder();
    }

    @Override
    protected Class<NumeralFlexion> getTipoFlexion() {
        return NumeralFlexion.class;
    }

    /**
     * Restringe el género del numeral.
     * Varios valores se interpretan como OR.
     *
     * @param generos valores aceptados
     * @return este builder
     */
    public NumeralCriterioBuilder conGenero(Genero... generos) {
        return agregarRestriccion("genero", (Object[]) generos);
    }

    /**
     * Restringe el número gramatical del numeral.
     * Varios valores se interpretan como OR.
     *
     * @param numeros valores aceptados
     * @return este builder
     */
    public NumeralCriterioBuilder conNumero(Numero... numeros) {
        return agregarRestriccion("numero", (Object[]) numeros);
    }

    /**
     * Restringe el caso gramatical del numeral.
     * Varios valores se interpretan como OR.
     *
     * @param casos valores aceptados
     * @return este builder
     */
    public NumeralCriterioBuilder conCaso(Caso... casos) {
        return agregarRestriccion("caso", (Object[]) casos);
    }

    // ============================================
    // Restricciones de cantidad (campo numérico de la palabra base)
    // ============================================

    /**
     * Restringe la cantidad del numeral a valores concretos.
     * Varios valores se interpretan como OR (ej. {@code conCantidad(1, 2, 3)} = 1 O 2 O 3).
     * <p>
     * La cantidad se obtiene del campo {@code Numeral.cantidad} (palabra base).
     *
     * @param cantidades valores aceptados
     * @return este builder
     */
    public NumeralCriterioBuilder conCantidad(int... cantidades) {
        java.util.Set<Integer> valores = new java.util.LinkedHashSet<>();
        for (int c : cantidades) {
            valores.add(c);
        }
        return agregarRestriccionNumerica(new RestriccionNumerica.Valores("base.cantidad", valores));
    }

    /**
     * Restringe la cantidad del numeral a un rango inclusivo [min, max].
     * <p>
     * Ejemplo: {@code cantidadEntre(1, 4)} acepta numerales con cantidad 1, 2, 3 o 4.
     *
     * @param min valor mínimo (inclusivo)
     * @param max valor máximo (inclusivo)
     * @return este builder
     */
    public NumeralCriterioBuilder cantidadEntre(int min, int max) {
        return agregarRestriccionNumerica(new RestriccionNumerica.Entre("base.cantidad", min, max));
    }

    /**
     * Restringe la cantidad del numeral a valores estrictamente mayores que el umbral.
     * <p>
     * Ejemplo: {@code conCantidadMayorQue(5)} acepta numerales con cantidad 6, 7, 8, ...
     *
     * @param cantidad umbral (exclusivo)
     * @return este builder
     */
    public NumeralCriterioBuilder conCantidadMayorQue(int cantidad) {
        return agregarRestriccionNumerica(new RestriccionNumerica.MayorQue("base.cantidad", cantidad));
    }

    /**
     * Restringe la cantidad del numeral a valores estrictamente menores que el umbral.
     * <p>
     * Ejemplo: {@code conCantidadMenorQue(5)} acepta numerales con cantidad 1, 2, 3, 4.
     *
     * @param cantidad umbral (exclusivo)
     * @return este builder
     */
    public NumeralCriterioBuilder conCantidadMenorQue(int cantidad) {
        return agregarRestriccionNumerica(new RestriccionNumerica.MenorQue("base.cantidad", cantidad));
    }
}
