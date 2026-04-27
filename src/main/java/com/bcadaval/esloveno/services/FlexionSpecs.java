package com.bcadaval.esloveno.services;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.palabra.*;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

/**
 * Specifications JPA reutilizables para consultas sobre entidades {@link PalabraFlexion}.
 * <p>
 * Contiene filtros genéricos aplicables a cualquier tipo de flexión (elegible, nueva, vencida, etc.)
 * y filtros específicos por tipo que acceden a campos de la palabra base.
 * <p>
 * Todas las Specifications son composables con {@code .and()}, {@code .or()}.
 */
public final class FlexionSpecs {

    private FlexionSpecs() {
        // Clase de utilidad, no instanciable
    }

    // ============================================
    // Specs genéricas (aplicables a cualquier tipo)
    // ============================================

    /**
     * Flexiones marcadas como elegibles para estudio.
     */
    public static <T> Specification<T> elegible() {
        return (root, query, cb) -> cb.equal(root.get("elegible"), true);
    }

    /**
     * Flexiones NO elegibles para estudio.
     */
    public static <T> Specification<T> noElegible() {
        return (root, query, cb) -> cb.equal(root.get("elegible"), false);
    }

    /**
     * Flexiones nuevas: nunca estudiadas ({@code ultimaRevision IS NULL}).
     */
    public static <T> Specification<T> nueva() {
        return (root, query, cb) -> cb.isNull(root.get("ultimaRevision"));
    }

    /**
     * Flexiones en estudio: ya estudiadas al menos una vez ({@code ultimaRevision IS NOT NULL}).
     */
    public static <T> Specification<T> enEstudio() {
        return (root, query, cb) -> cb.isNotNull(root.get("ultimaRevision"));
    }

    /**
     * Flexiones con próxima revisión programada ({@code proximaRevision IS NOT NULL}).
     */
    public static <T> Specification<T> conProximaRevision() {
        return (root, query, cb) -> cb.isNotNull(root.get("proximaRevision"));
    }

    /**
     * Flexiones sin próxima revisión ({@code proximaRevision IS NULL}).
     * Representan tarjetas nuevas que aún no han entrado al ciclo SRS.
     */
    public static <T> Specification<T> sinProximaRevision() {
        return (root, query, cb) -> cb.isNull(root.get("proximaRevision"));
    }

    /**
     * Flexiones con revisión vencida: su próxima revisión ha pasado o es ahora.
     *
     * @param ahora momento actual para la comparación
     */
    public static <T> Specification<T> vencida(Instant ahora) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("proximaRevision"), ahora);
    }

    /**
     * Flexiones listas para estudiar: elegibles Y (nueva sin proximaRevision O con revisión vencida).
     * <p>
     * Combina las condiciones necesarias para que una tarjeta aparezca en el estudio:
     * <ul>
     *   <li>Elegible = true</li>
     *   <li>proximaRevision IS NULL (nueva) OR proximaRevision &lt;= ahora (vencida)</li>
     * </ul>
     *
     * @param ahora momento actual para la comparación
     */
    public static <T> Specification<T> listaParaEstudiar(Instant ahora) {
        return Specification.where(FlexionSpecs.<T>elegible())
                .and(FlexionSpecs.<T>sinProximaRevision().or(FlexionSpecs.vencida(ahora)));
    }

    /**
     * Flexiones elegibles con revisión vencida (tarjetas de revisión, no nuevas).
     *
     * @param ahora momento actual para la comparación
     */
    public static <T> Specification<T> revisionVencida(Instant ahora) {
        return Specification.where(FlexionSpecs.<T>elegible())
                .and(FlexionSpecs.conProximaRevision())
                .and(FlexionSpecs.vencida(ahora));
    }

    /**
     * Flexiones elegibles y nuevas (sin proximaRevision).
     */
    public static <T> Specification<T> elegibleNueva() {
        return Specification.where(FlexionSpecs.<T>elegible())
                .and(FlexionSpecs.sinProximaRevision());
    }

    /**
     * Flexiones en reaprendizaje.
     */
    public static <T> Specification<T> enReaprendizaje() {
        return (root, query, cb) -> cb.equal(root.get("enReaprendizaje"), true);
    }

    // ============================================
    // Specs específicas por tipo: Verbo
    // ============================================

    /**
     * Verbo base completo: significado, transitividad, requiereSujetoAnimado y requiereObjetoAnimado no null.
     */
    public static Specification<VerboFlexion> verboBaseCompleto() {
        return (root, query, cb) -> {
            var base = root.join("verboBase", JoinType.INNER);
            return cb.and(
                    cb.isNotNull(base.get("significado")),
                    cb.isNotNull(base.get("transitividad")),
                    cb.isNotNull(base.get("requiereSujetoAnimado")),
                    cb.isNotNull(base.get("requiereObjetoAnimado"))
            );
        };
    }

    // ============================================
    // Specs específicas por tipo: Sustantivo
    // ============================================

    /**
     * Sustantivo base completo: significado, animacidad, contabilidad y claseSemantica no null.
     */
    public static Specification<SustantivoFlexion> sustantivoBaseCompleto() {
        return (root, query, cb) -> {
            var base = root.join("sustantivoBase", JoinType.INNER);
            return cb.and(
                    cb.isNotNull(base.get("significado")),
                    cb.isNotNull(base.get("animacidad")),
                    cb.isNotNull(base.get("contabilidad")),
                    cb.isNotNull(base.get("claseSemantica"))
            );
        };
    }

    // ============================================
    // Specs específicas por tipo: Adjetivo
    // ============================================

    /**
     * Adjetivo base completo: significado no null.
     */
    public static Specification<AdjetivoFlexion> adjetivoBaseCompleto() {
        return (root, query, cb) -> {
            var base = root.join("adjetivoBase", JoinType.INNER);
            return cb.isNotNull(base.get("significado"));
        };
    }

    // ============================================
    // Specs específicas por tipo: Pronombre
    // ============================================

    /**
     * Pronombre completo: significado no null (campo directo en la flexión).
     */
    public static Specification<PronombreFlexion> pronombreCompleto() {
        return (root, query, cb) -> cb.isNotNull(root.get("significado"));
    }

    // ============================================
    // Specs específicas por tipo: Numeral
    // ============================================

    /**
     * Numeral base completo: significado y cantidad no null.
     */
    public static Specification<NumeralFlexion> numeralBaseCompleto() {
        return (root, query, cb) -> {
            var base = root.join("numeralBase", JoinType.INNER);
            return cb.and(
                    cb.isNotNull(base.get("significado")),
                    cb.isNotNull(base.get("cantidad"))
            );
        };
    }

    // ============================================
    // Specs específicas por tipo: Partícula
    // ============================================

    /**
     * Partícula base completa: significado no null.
     */
    public static Specification<ParticulaFlexion> particulaBaseCompleta() {
        return (root, query, cb) -> {
            var base = root.join("particulaBase", JoinType.INNER);
            return cb.isNotNull(base.get("significado"));
        };
    }
}

