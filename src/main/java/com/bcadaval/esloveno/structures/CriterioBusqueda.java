package com.bcadaval.esloveno.structures;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

import java.util.function.Predicate;

/**
 * Criterio de búsqueda para un elemento de frase.
 * Encapsula tanto un Predicate (para validación en memoria)
 * como un Specification (para consultas JPA).
 *
 * @param <T> Tipo de PalabraFlexion que busca
 */
@Getter
public class CriterioBusqueda<T extends PalabraFlexion> {

    /**
     *  Clase del tipo de flexión que busca este criterio.
     */
    private final Class<T> tipoFlexion;
    /**
     *  Predicado para validar si una palabra cumple el criterio en memoria.
     */
    private final Predicate<T> predicado;
    /**
     *  Specification para consultas JPA.
     */
    private final Specification<T> specification;

    private CriterioBusqueda(Class<T> tipoFlexion, Predicate<T> predicado, Specification<T> specification) {
        this.tipoFlexion = tipoFlexion;
        this.predicado = predicado;
        this.specification = specification;
    }

    /**
     * Crea un criterio de búsqueda con predicado y specification.
     *
     * @param tipoFlexion Clase del tipo de flexión (ej: SustantivoFlexion.class)
     * @param predicado Predicado para validación en memoria
     * @param specification Specification para consultas JPA
     */
    public static <T extends PalabraFlexion> CriterioBusqueda<T> de(
            Class<T> tipoFlexion,
            Predicate<T> predicado,
            Specification<T> specification) {
        return new CriterioBusqueda<>(tipoFlexion, predicado, specification);
    }

    /**
     * Verifica si una PalabraFlexion cumple este criterio.
     * Primero comprueba el tipo, luego aplica el predicado.
     *
     * @param palabra Palabra a verificar
     * @return true si cumple el criterio
     */
    @SuppressWarnings("unchecked")
    public boolean cumple(PalabraFlexion palabra) {
        if (palabra == null) return false;
        if (!tipoFlexion.isInstance(palabra)) return false;
        return predicado.test((T) palabra);
    }
}

