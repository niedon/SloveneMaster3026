package com.bcadaval.esloveno.structures.extractores;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ExtractorSustantivo implements EstrategiaExtraccion<SustantivoFlexion> {

    public static <T extends PalabraFlexion<?>> EstrategiaExtraccion<T> get() {
        return (EstrategiaExtraccion<T>) new ExtractorSustantivo();
    }

    @Override
    public Function<SustantivoFlexion, String> deEspanol() {
        return PalabraFlexion::getSignificado;
    }

    @Override
    public Function<SustantivoFlexion, String> aEsloveno() {
        return p -> String.format("(%s) %s", p.getSustantivoBase().getGenero().getEmoji(), p.getAcentuado());
    }

    @Override
    public Function<SustantivoFlexion, String> deEsloveno() {
        return PalabraFlexion::getFlexion;
    }

    @Override
    public Function<SustantivoFlexion, String> aEspanol() {
        return p -> String.format("(%s) %s", p.getSustantivoBase().getGenero().getEmoji(), p.getSignificado());
    }
}
