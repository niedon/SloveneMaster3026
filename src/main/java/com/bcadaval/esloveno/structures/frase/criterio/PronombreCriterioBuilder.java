package com.bcadaval.esloveno.structures.frase.criterio;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;

/**
 * Builder tipado de {@link CriterioBusquedaNuevo} para {@link PronombreFlexion}.
 * <p>
 * Expone las características gramaticales relevantes para pronombres:
 * <ul>
 *   <li>{@link #conPersona(Persona...)} — Persona gramatical (campo de la flexión)</li>
 *   <li>{@link #conGenero(Genero...)} — Género (campo de la flexión)</li>
 *   <li>{@link #conNumero(Numero...)} — Número gramatical (campo de la flexión)</li>
 *   <li>{@link #conCaso(Caso...)} — Caso gramatical (campo de la flexión)</li>
 *   <li>{@link #conClitico(Boolean...)} — Si es clítico (campo Boolean de la flexión)</li>
 *   <li>{@link #conTipoPronombre(TipoPronombre...)} — Tipo de pronombre (campo de la palabra base {@code Pronombre})</li>
 * </ul>
 * <p>
 * Ejemplo de uso:
 * <pre>
 * CriterioBusquedaNuevo.de(PronombreFlexion.class)
 *     .conPersona(Persona.PRIMERA)
 *     .conCaso(Caso.NOMINATIVO)
 *     .conTipoPronombre(TipoPronombre.PERSONAL)
 *     .build();
 * </pre>
 */
public class PronombreCriterioBuilder extends CriterioBuilderBase<PronombreFlexion, PronombreCriterioBuilder> {

    /**
     * Factory method estático.
     */
    public static PronombreCriterioBuilder crear() {
        return new PronombreCriterioBuilder();
    }

    @Override
    protected Class<PronombreFlexion> getTipoFlexion() {
        return PronombreFlexion.class;
    }

    /**
     * Restringe la persona gramatical del pronombre.
     * Varios valores se interpretan como OR.
     *
     * @param personas valores aceptados
     * @return este builder
     */
    public PronombreCriterioBuilder conPersona(Persona... personas) {
        return agregarRestriccion("persona", (Object[]) personas);
    }

    /**
     * Restringe el género del pronombre.
     * Varios valores se interpretan como OR.
     *
     * @param generos valores aceptados
     * @return este builder
     */
    public PronombreCriterioBuilder conGenero(Genero... generos) {
        return agregarRestriccion("genero", (Object[]) generos);
    }

    /**
     * Restringe el número gramatical del pronombre.
     * Varios valores se interpretan como OR.
     *
     * @param numeros valores aceptados
     * @return este builder
     */
    public PronombreCriterioBuilder conNumero(Numero... numeros) {
        return agregarRestriccion("numero", (Object[]) numeros);
    }

    /**
     * Restringe el caso gramatical del pronombre.
     * Varios valores se interpretan como OR.
     *
     * @param casos valores aceptados
     * @return este builder
     */
    public PronombreCriterioBuilder conCaso(Caso... casos) {
        return agregarRestriccion("caso", (Object[]) casos);
    }

    /**
     * Restringe si el pronombre es clítico.
     * Varios valores se interpretan como OR.
     *
     * @param clitico valores aceptados ({@code true} = clítico, {@code false} = no clítico)
     * @return este builder
     */
    public PronombreCriterioBuilder conClitico(Boolean... clitico) {
        return agregarRestriccion("clitico", (Object[]) clitico);
    }

    /**
     * Restringe el tipo de pronombre (campo de la palabra base {@code Pronombre}).
     * Varios valores se interpretan como OR.
     *
     * @param tipos valores aceptados
     * @return este builder
     */
    public PronombreCriterioBuilder conTipoPronombre(TipoPronombre... tipos) {
        return agregarRestriccion("base.tipoPronombre", (Object[]) tipos);
    }
}
