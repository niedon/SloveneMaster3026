package com.bcadaval.esloveno.structures.frase.criterio;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;

/**
 * Builder tipado de {@link CriterioBusquedaNuevo} para {@link VerboFlexion}.
 * <p>
 * Expone las características gramaticales relevantes para verbos:
 * <ul>
 *   <li>{@link #conFormaVerbal(FormaVerbal...)} — Forma verbal (campo de la flexión)</li>
 *   <li>{@link #conPersona(Persona...)} — Persona gramatical (campo de la flexión)</li>
 *   <li>{@link #conNumero(Numero...)} — Número gramatical (campo de la flexión)</li>
 *   <li>{@link #conGenero(Genero...)} — Género (campo de la flexión, solo para participios)</li>
 *   <li>{@link #conNegativo(Boolean...)} — Negación (campo Boolean de la flexión)</li>
 *   <li>{@link #conTransitividad(Transitividad...)} — Transitividad (campo de la palabra base {@code Verbo})</li>
 *   <li>{@link #conAspecto(Aspecto...)} — Aspecto verbal (campo de la palabra base {@code Verbo})</li>
 * </ul>
 * <p>
 * Ejemplo de uso:
 * <pre>
 * CriterioBusquedaNuevo.de(VerboFlexion.class)
 *     .conFormaVerbal(FormaVerbal.PRESENT)
 *     .conTransitividad(Transitividad.TRANSITIVO)
 *     .conNegativo(false)
 *     .build();
 * </pre>
 */
public class VerboCriterioBuilder extends CriterioBuilderBase<VerboFlexion, VerboCriterioBuilder> {

    /**
     * Factory method estático.
     * Uso: {@code VerboCriterioBuilder.crear().conFormaVerbal(...).build()}
     */
    public static VerboCriterioBuilder crear() {
        return new VerboCriterioBuilder();
    }

    @Override
    protected Class<VerboFlexion> getTipoFlexion() {
        return VerboFlexion.class;
    }

    /**
     * Restringe la forma verbal.
     * Varios valores se interpretan como OR.
     *
     * @param formas valores aceptados
     * @return este builder
     */
    public VerboCriterioBuilder conFormaVerbal(FormaVerbal... formas) {
        return agregarRestriccion("formaVerbal", (Object[]) formas);
    }

    /**
     * Restringe la persona gramatical.
     * Varios valores se interpretan como OR.
     *
     * @param personas valores aceptados
     * @return este builder
     */
    public VerboCriterioBuilder conPersona(Persona... personas) {
        return agregarRestriccion("persona", (Object[]) personas);
    }

    /**
     * Restringe el número gramatical.
     * Varios valores se interpretan como OR.
     *
     * @param numeros valores aceptados
     * @return este builder
     */
    public VerboCriterioBuilder conNumero(Numero... numeros) {
        return agregarRestriccion("numero", (Object[]) numeros);
    }

    /**
     * Restringe el género (aplica solo a participios).
     * Varios valores se interpretan como OR.
     *
     * @param generos valores aceptados
     * @return este builder
     */
    public VerboCriterioBuilder conGenero(Genero... generos) {
        return agregarRestriccion("genero", (Object[]) generos);
    }

    /**
     * Restringe la negación del verbo.
     * Varios valores se interpretan como OR.
     *
     * @param negativo valores aceptados ({@code true} = negativo, {@code false} = afirmativo)
     * @return este builder
     */
    public VerboCriterioBuilder conNegativo(Boolean... negativo) {
        return agregarRestriccion("negativo", (Object[]) negativo);
    }

    /**
     * Restringe la transitividad (campo de la palabra base {@code Verbo}).
     * Varios valores se interpretan como OR.
     *
     * @param transitividades valores aceptados
     * @return este builder
     */
    public VerboCriterioBuilder conTransitividad(Transitividad... transitividades) {
        return agregarRestriccion("base.transitividad", (Object[]) transitividades);
    }

    /**
     * Restringe el aspecto verbal (campo de la palabra base {@code Verbo}).
     * Varios valores se interpretan como OR.
     *
     * @param aspectos valores aceptados
     * @return este builder
     */
    public VerboCriterioBuilder conAspecto(Aspecto... aspectos) {
        return agregarRestriccion("base.aspecto", (Object[]) aspectos);
    }

    /**
     * Restringe si el verbo requiere sujeto animado (campo de la palabra base {@code Verbo}).
     * Varios valores se interpretan como OR.
     *
     * @param valores valores aceptados
     * @return este builder
     */
    public VerboCriterioBuilder conRequiereSujetoAnimado(RequiereSujetoAnimado... valores) {
        return agregarRestriccion("base.requiereSujetoAnimado", (Object[]) valores);
    }

    /**
     * Restringe si el verbo requiere objeto animado (campo de la palabra base {@code Verbo}).
     * Varios valores se interpretan como OR.
     *
     * @param valores valores aceptados
     * @return este builder
     */
    public VerboCriterioBuilder conRequiereObjetoAnimado(RequiereObjetoAnimado... valores) {
        return agregarRestriccion("base.requiereObjetoAnimado", (Object[]) valores);
    }
}


