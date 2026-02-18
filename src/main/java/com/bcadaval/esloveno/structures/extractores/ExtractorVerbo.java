package com.bcadaval.esloveno.structures.extractores;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ExtractorVerbo implements EstrategiaExtraccion<VerboFlexion> {

    public static <T extends PalabraFlexion<?>> EstrategiaExtraccion<T> get() {
        return (EstrategiaExtraccion<T>) new ExtractorVerbo();
    }

    @Override
    public Function<VerboFlexion, String> deEspanol() {
        return p -> String.format("(%s) %s", p.getVerboBase().getAspecto().getEmoji(), p.getSignificado());
    }

    @Override
    public Function<VerboFlexion, String> aEsloveno() {
        return PalabraFlexion::getAcentuado;
    }

    @Override
    public Function<VerboFlexion, String> deEsloveno() {
        return PalabraFlexion::getFlexion;
    }

    @Override
    public Function<VerboFlexion, String> aEspanol() {
        return p -> String.format("(%s) %s", p.getVerboBase().getAspecto().getEmoji(), p.getSignificado());
    }
}
