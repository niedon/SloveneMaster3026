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
}
