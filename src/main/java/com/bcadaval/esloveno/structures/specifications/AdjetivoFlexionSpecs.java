package com.bcadaval.esloveno.structures.specifications;

import com.bcadaval.esloveno.beans.enums.*;
import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications JPA para consultas de AdjetivoFlexion.
 */
public final class AdjetivoFlexionSpecs {

    private AdjetivoFlexionSpecs() {}

    /**
     * Specification para filtrar por género.
     */
    public static Specification<AdjetivoFlexion> conGenero(Genero genero) {
        return (root, query, cb) -> cb.equal(root.get("genero"), genero);
    }

    /**
     * Specification para filtrar por número.
     */
    public static Specification<AdjetivoFlexion> conNumero(Numero numero) {
        return (root, query, cb) -> cb.equal(root.get("numero"), numero);
    }

    /**
     * Specification para filtrar por caso.
     */
    public static Specification<AdjetivoFlexion> conCaso(Caso caso) {
        return (root, query, cb) -> cb.equal(root.get("caso"), caso);
    }

    /**
     * Specification para filtrar por grado.
     */
    public static Specification<AdjetivoFlexion> conGrado(Grado grado) {
        return (root, query, cb) -> cb.equal(root.get("grado"), grado);
    }

    /**
     * Specification para filtrar por definitud.
     */
    public static Specification<AdjetivoFlexion> conDefinitud(Definitud definitud) {
        return (root, query, cb) -> cb.equal(root.get("definitud"), definitud);
    }

    /**
     * Specification para asegurar que tiene adjetivo base (no huérfano).
     */
    public static Specification<AdjetivoFlexion> conAdjetivoBase() {
        return (root, query, cb) -> cb.isNotNull(root.get("adjetivoBase"));
    }

    /**
     * Specification combinada: caso + género + adjetivo base.
     */
    public static Specification<AdjetivoFlexion> conCasoGeneroYBase(Caso caso, Genero genero) {
        return conCaso(caso).and(conGenero(genero)).and(conAdjetivoBase());
    }

    /**
     * Specification combinada: caso + número + adjetivo base.
     */
    public static Specification<AdjetivoFlexion> conCasoNumeroYBase(Caso caso, Numero numero) {
        return conCaso(caso).and(conNumero(numero)).and(conAdjetivoBase());
    }

    /**
     * Specification combinada: caso + género + número + adjetivo base.
     */
    public static Specification<AdjetivoFlexion> conCasoGeneroNumeroYBase(Caso caso, Genero genero, Numero numero) {
        return conCaso(caso)
                .and(conGenero(genero))
                .and(conNumero(numero))
                .and(conAdjetivoBase());
    }

    /**
     * Specification combinada: grado + adjetivo base.
     */
    public static Specification<AdjetivoFlexion> conGradoYBase(Grado grado) {
        return conGrado(grado).and(conAdjetivoBase());
    }

    /**
     * Specification para adjetivos con información completa para estudio SRS.
     * Requiere: adjetivoBase no null, significado no null.
     */
    public static Specification<AdjetivoFlexion> completaParaEstudio() {
        return (root, query, cb) -> cb.and(
                cb.isNotNull(root.get("adjetivoBase")),
                cb.isNotNull(root.get("adjetivoBase").get("significado"))
        );
    }
}

