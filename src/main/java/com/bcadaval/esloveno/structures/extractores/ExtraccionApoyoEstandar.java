package com.bcadaval.esloveno.structures.extractores;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import org.springframework.stereotype.Component;

/**
 * Extracción estándar para elementos de apoyo (pronombres, numerales, etc.).
 * <p>
 * Patrón:
 * - ES→SL: significado → flexion
 * - SL→ES: flexion → significado
 * <p>
 * Bean singleton gestionado por Spring.
 */
@Component
public class ExtraccionApoyoEstandar implements EstrategiaExtraccion {


    @Override
    public String deEspanol(PalabraFlexion<?> palabra) {
        return palabra.getSignificado();
    }

    @Override
    public String aEsloveno(PalabraFlexion<?> palabra) {
        return palabra.getFlexion();
    }

    @Override
    public String deEsloveno(PalabraFlexion<?> palabra) {
        return palabra.getFlexion();
    }

    @Override
    public String aEspanol(PalabraFlexion<?> palabra) {
        return palabra.getSignificado();
    }
}

