package com.bcadaval.esloveno.structures.extractores;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ExtractorPronombre implements EstrategiaExtraccion<PalabraFlexion<?>> {
    /**
     * Función estática para obtener una instancia tipada a un tipo específico.
     * Seguro porque todos los extractores trabajan con métodos de PalabraFlexion.
     */
    @SuppressWarnings("unchecked")
    public static <T extends PalabraFlexion<?>> EstrategiaExtraccion<T> get() {
        return (EstrategiaExtraccion<T>) new ExtractorPronombre();
    }

    @Override
    public Function<PalabraFlexion<?>, String> deEspanol() {
        return PalabraFlexion::getSignificado;
    }

    @Override
    public Function<PalabraFlexion<?>, String> aEsloveno() {
        return PalabraFlexion::getAcentuado;
    }

    @Override
    public Function<PalabraFlexion<?>, String> deEsloveno() {
        return p -> "";
    }

    @Override
    public Function<PalabraFlexion<?>, String> aEspanol() {
        return PalabraFlexion::getSignificado;
    }

}
