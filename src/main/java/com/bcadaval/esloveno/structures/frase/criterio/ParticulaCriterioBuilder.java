package com.bcadaval.esloveno.structures.frase.criterio;

import com.bcadaval.esloveno.beans.palabra.ParticulaFlexion;

/**
 * Builder tipado de {@link CriterioBusquedaNuevo} para {@link ParticulaFlexion}.
 * <p>
 * Las partículas son invariables y no tienen características gramaticales propias
 * (no tienen género, número, caso, etc.). Los criterios relevantes son:
 * <ul>
 *   <li>{@link #conPrincipal(String...)} — Filtrar por forma principal (heredado de base)</li>
 *   <li>{@link #conPrincipalExcepto(String...)} — Excluir por forma principal (heredado de base)</li>
 * </ul>
 * <p>
 * Ejemplo de uso:
 * <pre>
 * CriterioBusquedaNuevo.de(ParticulaFlexion.class)
 *     .conPrincipal("ne")
 *     .build();
 * </pre>
 */
public class ParticulaCriterioBuilder extends CriterioBuilderBase<ParticulaFlexion, ParticulaCriterioBuilder> {

    /**
     * Factory method estático.
     */
    public static ParticulaCriterioBuilder crear() {
        return new ParticulaCriterioBuilder();
    }

    @Override
    protected Class<ParticulaFlexion> getTipoFlexion() {
        return ParticulaFlexion.class;
    }
}

