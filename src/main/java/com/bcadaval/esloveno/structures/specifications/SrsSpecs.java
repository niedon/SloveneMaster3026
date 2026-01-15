package com.bcadaval.esloveno.structures.specifications;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

/**
 * Specifications JPA para criterios del Sistema de Repetición Espaciada (SRS).
 * Aplicables a cualquier tipo de PalabraFlexion.
 * <p>
 * Estos criterios determinan qué tarjetas están disponibles para estudio
 * independientemente de su tipo gramatical.
 */
public final class SrsSpecs {

    private SrsSpecs() {}

    /**
     * Specification para tarjetas listas para revisar.
     * proximaRevision <= ahora (ya pasó su tiempo de espera)
     */
    public static <T extends PalabraFlexion<?>> Specification<T> listaParaRevisar() {
        return (root, query, cb) -> cb.lessThanOrEqualTo(
                root.get("proximaRevision"),
                Instant.now()
        );
    }

    /**
     * Specification para tarjetas nuevas (nunca estudiadas).
     * proximaRevision == null
     */
    public static <T extends PalabraFlexion<?>> Specification<T> nueva() {
        return (root, query, cb) -> cb.isNull(root.get("proximaRevision"));
    }

    /**
     * Specification para tarjetas en reaprendizaje.
     * enReaprendizaje == true
     */
    public static <T extends PalabraFlexion<?>> Specification<T> enReaprendizaje() {
        return (root, query, cb) -> cb.equal(root.get("enReaprendizaje"), true);
    }

    /**
     * Specification para tarjetas disponibles para estudio.
     * Incluye: nuevas O listas para revisar.
     */
    public static <T extends PalabraFlexion<?>> Specification<T> disponibleParaEstudio() {
        return SrsSpecs.<T>nueva().or(SrsSpecs.<T>listaParaRevisar());
    }

    /**
     * Specification para ordenar por prioridad SRS:
     * 1. En reaprendizaje primero
     * 2. Luego por proximaRevision más antigua
     * <p>
     * NOTA: El ordenamiento se aplica en la query, no como Specification.
     * Usar con repository.findAll(spec, Sort.by(...))
     */
    public static <T extends PalabraFlexion<?>> Specification<T> ordenadoPorPrioridad() {
        return (root, query, cb) -> {
            if (query != null) {
                query.orderBy(
                        cb.desc(root.get("enReaprendizaje")),
                        cb.asc(root.get("proximaRevision"))
                );
            }
            return cb.conjunction();
        };
    }
}

