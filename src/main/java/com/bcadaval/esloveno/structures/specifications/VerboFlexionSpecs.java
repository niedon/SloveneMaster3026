package com.bcadaval.esloveno.structures.specifications;

import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.Numero;
import com.bcadaval.esloveno.beans.enums.Persona;
import com.bcadaval.esloveno.beans.enums.Transitividad;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications JPA para consultas de VerboFlexion.
 */
public final class VerboFlexionSpecs {

    private VerboFlexionSpecs() {}

    /**
     * Specification para filtrar por forma verbal.
     */
    public static Specification<VerboFlexion> conFormaVerbal(FormaVerbal formaVerbal) {
        return (root, query, cb) -> cb.equal(root.get("formaVerbal"), formaVerbal);
    }

    /**
     * Specification para filtrar por persona.
     */
    public static Specification<VerboFlexion> conPersona(Persona persona) {
        return (root, query, cb) -> cb.equal(root.get("persona"), persona);
    }

    /**
     * Specification para filtrar por número.
     */
    public static Specification<VerboFlexion> conNumero(Numero numero) {
        return (root, query, cb) -> cb.equal(root.get("numero"), numero);
    }

    /**
     * Specification para asegurar que tiene verbo base (no huérfano).
     */
    public static Specification<VerboFlexion> conVerboBase() {
        return (root, query, cb) -> cb.isNotNull(root.get("verboBase"));
    }

    /**
     * Specification para filtrar por transitividad del verbo base.
     */
    public static Specification<VerboFlexion> conTransitividad(Transitividad transitividad) {
        return (root, query, cb) -> cb.equal(root.get("verboBase").get("transitividad"), transitividad);
    }

    /**
     * Specification combinada: forma verbal + verbo base.
     */
    public static Specification<VerboFlexion> conFormaVerbalYBase(FormaVerbal formaVerbal) {
        return conFormaVerbal(formaVerbal).and(conVerboBase());
    }

    /**
     * Specification combinada: forma verbal + transitividad + verbo base.
     */
    public static Specification<VerboFlexion> conFormaVerbalTransitividadYBase(
            FormaVerbal formaVerbal, Transitividad transitividad) {
        return conFormaVerbal(formaVerbal)
                .and(conTransitividad(transitividad))
                .and(conVerboBase());
    }

    /**
     * Specification combinada: forma verbal + persona + número + verbo base.
     */
    public static Specification<VerboFlexion> conFormaVerbalPersonaNumeroYBase(
            FormaVerbal formaVerbal, Persona persona, Numero numero) {
        return conFormaVerbal(formaVerbal)
                .and(conPersona(persona))
                .and(conNumero(numero))
                .and(conVerboBase());
    }

    /**
     * Specification para verbos con información completa para estudio SRS.
     * Requiere: verboBase no null, significado no null, transitividad no null.
     */
    public static Specification<VerboFlexion> completaParaEstudio() {
        return (root, query, cb) -> cb.and(
                cb.isNotNull(root.get("verboBase")),
                cb.isNotNull(root.get("verboBase").get("significado")),
                cb.isNotNull(root.get("verboBase").get("transitividad"))
        );
    }
}

