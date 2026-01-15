package com.bcadaval.esloveno.structures.specifications;

import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications JPA para consultas de SustantivoFlexion.
 */
public final class SustantivoFlexionSpecs {

    private SustantivoFlexionSpecs() {}

    /**
     * Specification para filtrar por caso.
     */
    public static Specification<SustantivoFlexion> conCaso(Caso caso) {
        return (root, query, cb) -> cb.equal(root.get("caso"), caso);
    }

    /**
     * Specification para filtrar por número.
     */
    public static Specification<SustantivoFlexion> conNumero(Numero numero) {
        return (root, query, cb) -> cb.equal(root.get("numero"), numero);
    }

    /**
     * Specification para asegurar que tiene sustantivo base (no huérfano).
     */
    public static Specification<SustantivoFlexion> conSustantivoBase() {
        return (root, query, cb) -> cb.isNotNull(root.get("sustantivoBase"));
    }

    /**
     * Specification combinada: caso + sustantivo base.
     */
    public static Specification<SustantivoFlexion> conCasoYBase(Caso caso) {
        return conCaso(caso).and(conSustantivoBase());
    }

    /**
     * Specification combinada: caso + número + sustantivo base.
     */
    public static Specification<SustantivoFlexion> conCasoNumeroYBase(Caso caso, Numero numero) {
        return conCaso(caso).and(conNumero(numero)).and(conSustantivoBase());
    }

    /**
     * Specification para sustantivos con información completa para estudio SRS.
     * Requiere: sustantivoBase no null, significado no null, animado no null.
     */
    public static Specification<SustantivoFlexion> completaParaEstudio() {
        return (root, query, cb) -> cb.and(
                cb.isNotNull(root.get("sustantivoBase")),
                cb.isNotNull(root.get("sustantivoBase").get("significado")),
                cb.isNotNull(root.get("sustantivoBase").get("animado"))
        );
    }
}

