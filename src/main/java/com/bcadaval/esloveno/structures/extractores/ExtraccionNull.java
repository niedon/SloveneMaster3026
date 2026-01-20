package com.bcadaval.esloveno.structures.extractores;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;

import java.util.function.Function;

public class ExtraccionNull implements EstrategiaExtraccion<PalabraFlexion<?>> {

    public static <T extends PalabraFlexion<?>> EstrategiaExtraccion<T> get() {
        return (EstrategiaExtraccion<T>) new ExtraccionNull();
    }

    @Override
    public Function<PalabraFlexion<?>, String> deEspanol() {
        return null;
    }

    @Override
    public Function<PalabraFlexion<?>, String> aEsloveno() {
        return null;
    }

    @Override
    public Function<PalabraFlexion<?>, String> deEsloveno() {
        return null;
    }

    @Override
    public Function<PalabraFlexion<?>, String> aEspanol() {
        return null;
    }
}
