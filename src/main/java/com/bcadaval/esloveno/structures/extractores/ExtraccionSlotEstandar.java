package com.bcadaval.esloveno.structures.extractores;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import org.springframework.stereotype.Component;

import java.util.function.Function;

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
public class ExtraccionSlotEstandar implements EstrategiaExtraccion<PalabraFlexion<?>> {

    /**
     * Función estática para obtener una instancia tipada a un tipo específico.
     * Seguro porque todos los extractores trabajan con métodos de PalabraFlexion.
     */
    @SuppressWarnings("unchecked")
    public static <T extends PalabraFlexion<?>> EstrategiaExtraccion<T> get() {
        return (EstrategiaExtraccion<T>) new ExtraccionSlotEstandar();
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
        return PalabraFlexion::getFlexion;
    }

    @Override
    public Function<PalabraFlexion<?>, String> aEspanol() {
        return PalabraFlexion::getSignificado;
    }
}

