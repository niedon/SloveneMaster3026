package com.bcadaval.esloveno.structures.frase.criterio;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;

/**
 * Builder tipado de {@link CriterioBusquedaNuevo} para {@link SustantivoFlexion}.
 * <p>
 * Expone únicamente las características gramaticales relevantes para sustantivos:
 * <ul>
 *   <li>{@link #conNumero(Numero...)} — Número gramatical (campo de la flexión)</li>
 *   <li>{@link #conCaso(Caso...)} — Caso gramatical (campo de la flexión)</li>
 *   <li>{@link #conGenero(Genero...)} — Género (campo de la palabra base {@code Sustantivo})</li>
 *   <li>{@link #conAnimacidad(Animacidad...)} — Animacidad (campo de la palabra base {@code Sustantivo})</li>
 *   <li>{@link #conContabilidad(Contabilidad...)} — Contabilidad (campo de la palabra base {@code Sustantivo})</li>
 *   <li>{@link #conClaseSemantica(ClaseSemantica...)} — Clase semántica (campo de la palabra base {@code Sustantivo})</li>
 *   <li>{@link #conCabezaRelacional(CabezaRelacional...)} — Cabeza relacional (campo de la palabra base {@code Sustantivo})</li>
 * </ul>
 * <p>
 * Ejemplo de uso:
 * <pre>
 * CriterioBusquedaNuevo.de(SustantivoFlexion.class)
 *     .conCaso(Caso.NOMINATIVO)
 *     .conGenero(Genero.FEMENINO, Genero.NEUTRO)
 *     .conAnimacidad(Animacidad.ANIMADO)
 *     .build();
 * </pre>
 */
public class SustantivoCriterioBuilder extends CriterioBuilderBase<SustantivoFlexion, SustantivoCriterioBuilder> {

    /**
     * Factory method estático como alternativa fluida.
     * Uso: {@code SustantivoCriterioBuilder.crear().conCaso(...).build()}
     */
    public static SustantivoCriterioBuilder crear() {
        return new SustantivoCriterioBuilder();
    }

    @Override
    protected Class<SustantivoFlexion> getTipoFlexion() {
        return SustantivoFlexion.class;
    }

    /**
     * Restringe el número gramatical del sustantivo.
     * Varios valores se interpretan como OR (ej. singular O plural).
     *
     * @param numeros valores aceptados
     * @return este builder
     */
    public SustantivoCriterioBuilder conNumero(Numero... numeros) {
        return agregarRestriccion("numero", (Object[]) numeros);
    }

    /**
     * Restringe el caso gramatical del sustantivo.
     * Varios valores se interpretan como OR.
     *
     * @param casos valores aceptados
     * @return este builder
     */
    public SustantivoCriterioBuilder conCaso(Caso... casos) {
        return agregarRestriccion("caso", (Object[]) casos);
    }

    /**
     * Restringe el género del sustantivo (campo de la palabra base {@code Sustantivo}).
     * Varios valores se interpretan como OR.
     *
     * @param generos valores aceptados
     * @return este builder
     */
    public SustantivoCriterioBuilder conGenero(Genero... generos) {
        return agregarRestriccion("base.genero", (Object[]) generos);
    }

    /**
     * Restringe la animacidad del sustantivo (campo de la palabra base {@code Sustantivo}).
     * Varios valores se interpretan como OR.
     *
     * @param animacidades valores aceptados
     * @return este builder
     */
    public SustantivoCriterioBuilder conAnimacidad(Animacidad... animacidades) {
        return agregarRestriccion("base.animacidad", (Object[]) animacidades);
    }

    /**
     * Restringe la contabilidad del sustantivo (campo de la palabra base {@code Sustantivo}).
     * Varios valores se interpretan como OR.
     *
     * @param contabilidades valores aceptados
     * @return este builder
     */
    public SustantivoCriterioBuilder conContabilidad(Contabilidad... contabilidades) {
        return agregarRestriccion("base.contabilidad", (Object[]) contabilidades);
    }

    /**
     * Restringe la clase semántica del sustantivo (campo de la palabra base {@code Sustantivo}).
     * Varios valores se interpretan como OR.
     *
     * @param clases valores aceptados
     * @return este builder
     */
    public SustantivoCriterioBuilder conClaseSemantica(ClaseSemantica... clases) {
        return agregarRestriccion("base.claseSemantica", (Object[]) clases);
    }

    /**
     * Restringe si el sustantivo es cabeza relacional (campo de la palabra base {@code Sustantivo}).
     * Varios valores se interpretan como OR.
     *
     * @param valores valores aceptados
     * @return este builder
     */
    public SustantivoCriterioBuilder conCabezaRelacional(CabezaRelacional... valores) {
        return agregarRestriccion("base.cabezaRelacional", (Object[]) valores);
    }
}
