package com.bcadaval.esloveno.structures.frase.criterio;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;

/**
 * Builder tipado de {@link CriterioBusquedaNuevo} para {@link AdjetivoFlexion}.
 * <p>
 * Expone las características gramaticales relevantes para adjetivos:
 * <ul>
 *   <li>{@link #conGenero(Genero...)} — Género (campo de la flexión)</li>
 *   <li>{@link #conNumero(Numero...)} — Número gramatical (campo de la flexión)</li>
 *   <li>{@link #conCaso(Caso...)} — Caso gramatical (campo de la flexión)</li>
 *   <li>{@link #conGrado(Grado...)} — Grado del adjetivo (campo de la flexión)</li>
 *   <li>{@link #conDefinitud(Definitud...)} — Definitud (campo de la flexión)</li>
 * </ul>
 * <p>
 * Ejemplo de uso:
 * <pre>
 * CriterioBusquedaNuevo.de(AdjetivoFlexion.class)
 *     .conCaso(Caso.NOMINATIVO)
 *     .conGrado(Grado.POSITIVO)
 *     .build();
 * </pre>
 */
@SuppressWarnings("unused")
public class AdjetivoCriterioBuilder extends CriterioBuilderBase<AdjetivoFlexion, AdjetivoCriterioBuilder> {

    /**
     * Factory method estático.
     */
    public static AdjetivoCriterioBuilder crear() {
        return new AdjetivoCriterioBuilder();
    }

    @Override
    protected Class<AdjetivoFlexion> getTipoFlexion() {
        return AdjetivoFlexion.class;
    }

    /**
     * Restringe el género del adjetivo.
     * Varios valores se interpretan como OR.
     *
     * @param generos valores aceptados
     * @return este builder
     */
    public AdjetivoCriterioBuilder conGenero(Genero... generos) {
        return agregarRestriccion("genero", (Object[]) generos);
    }

    /**
     * Restringe el número gramatical del adjetivo.
     * Varios valores se interpretan como OR.
     *
     * @param numeros valores aceptados
     * @return este builder
     */
    public AdjetivoCriterioBuilder conNumero(Numero... numeros) {
        return agregarRestriccion("numero", (Object[]) numeros);
    }

    /**
     * Restringe el caso gramatical del adjetivo.
     * Varios valores se interpretan como OR.
     *
     * @param casos valores aceptados
     * @return este builder
     */
    public AdjetivoCriterioBuilder conCaso(Caso... casos) {
        return agregarRestriccion("caso", (Object[]) casos);
    }

    /**
     * Restringe el grado del adjetivo.
     * Varios valores se interpretan como OR.
     *
     * @param grados valores aceptados
     * @return este builder
     */
    public AdjetivoCriterioBuilder conGrado(Grado... grados) {
        return agregarRestriccion("grado", (Object[]) grados);
    }

    /**
     * Restringe la definitud del adjetivo.
     * Varios valores se interpretan como OR.
     *
     * @param definitudes valores aceptados
     * @return este builder
     */
    public AdjetivoCriterioBuilder conDefinitud(Definitud... definitudes) {
        return agregarRestriccion("definitud", (Object[]) definitudes);
    }
}
