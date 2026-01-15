package com.bcadaval.esloveno.structures.extractores;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import org.springframework.stereotype.Component;

/**
 * Extracción estándar para slots principales (palabras con SRS).
 * <p>
 * Patrón:
 * - ES→SL: significado → acentuado
 * - SL→ES: flexion → significado
 * <p>
 * Bean singleton gestionado por Spring.
 */
@Component
public class ExtraccionSlotEstandar implements EstrategiaExtraccion {


    @Override
    public String deEspanol(PalabraFlexion<?> palabra) {
        return palabra.getSignificado();
    }

    @Override
    public String aEsloveno(PalabraFlexion<?> palabra) {
        return palabra.getAcentuado();
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

