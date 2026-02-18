package com.bcadaval.esloveno.structures.extractores;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ExtractorNumero implements EstrategiaExtraccion<PalabraFlexion<?>> {
    @SuppressWarnings("unchecked")
    public static <T extends PalabraFlexion<?>> EstrategiaExtraccion<T> get() {
        return (EstrategiaExtraccion<T>) new ExtractorNumero();
    }

    @Override
    public Function<PalabraFlexion<?>, String> deEspanol() {
        return PalabraFlexion::getSignificado;
    }

    @Override
    public Function<PalabraFlexion<?>, String> aEsloveno() {
        return PalabraFlexion::getFlexion;
    }

    @Override
    public Function<PalabraFlexion<?>, String> deEsloveno() {
        return p -> "nº";
    }

    @Override
    public Function<PalabraFlexion<?>, String> aEspanol() {
        return PalabraFlexion::getSignificado;
    }
}
