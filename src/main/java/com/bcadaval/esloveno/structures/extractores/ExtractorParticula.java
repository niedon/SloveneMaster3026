package com.bcadaval.esloveno.structures.extractores;

import com.bcadaval.esloveno.beans.palabra.ParticulaFlexion;

import java.util.function.Function;

/**
 * Extracción para partículas en las frases.
 * <p>
 * Patrón:
 * - ES→SL: significado → flexion
 * - SL→ES: flexion → significado
 */
public class ExtractorParticula implements EstrategiaExtraccion<ParticulaFlexion> {

    private static final ExtractorParticula INSTANCE = new ExtractorParticula();

    @SuppressWarnings("unchecked")
    public static <T extends ParticulaFlexion> EstrategiaExtraccion<T> get() {
        return (EstrategiaExtraccion<T>) INSTANCE;
    }

    @Override
    public Function<ParticulaFlexion, String> deEspanol() {
        return ParticulaFlexion::getSignificado;
    }

    @Override
    public Function<ParticulaFlexion, String> aEsloveno() {
        return ParticulaFlexion::getAcentuado;
    }

    @Override
    public Function<ParticulaFlexion, String> deEsloveno() {
        return ParticulaFlexion::getFlexion;
    }

    @Override
    public Function<ParticulaFlexion, String> aEspanol() {
        return ParticulaFlexion::getSignificado;
    }
}

