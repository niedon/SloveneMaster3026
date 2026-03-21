package com.bcadaval.esloveno.services;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.palabra.*;
import com.bcadaval.esloveno.structures.frase.criterio.CriterioBusquedaNuevo;
import com.bcadaval.esloveno.structures.frase.criterio.RestriccionNumerica;
import jakarta.persistence.criteria.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Convierte un {@link CriterioBusquedaNuevo} (con restricciones de igualdad,
 * exclusión y numéricas) en una {@link Specification} JPA que puede ejecutarse
 * directamente contra la base de datos.
 * <p>
 * Maneja:
 * <ul>
 *   <li>Campos directos de la flexión (ej. {@code caso}, {@code numero})</li>
 *   <li>Campos de la palabra base con prefijo {@code base.} (ej. {@code base.genero} → join a sustantivoBase.genero)</li>
 *   <li>Restricciones de exclusión con prefijo {@code !} (ej. {@code !principal})</li>
 *   <li>Restricciones numéricas: {@link RestriccionNumerica.Valores}, {@link RestriccionNumerica.Entre},
 *       {@link RestriccionNumerica.MayorQue}, {@link RestriccionNumerica.MenorQue}</li>
 * </ul>
 */
@Log4j2
@Component
public class CriterioToSpecificationConverter {

    /**
     * Mapa de tipo de flexión → nombre del atributo JPA que referencia la palabra base.
     */
    private static final Map<Class<? extends PalabraFlexion<?>>, String> NOMBRE_RELACION_BASE = Map.of(
            SustantivoFlexion.class, "sustantivoBase",
            VerboFlexion.class, "verboBase",
            AdjetivoFlexion.class, "adjetivoBase",
            NumeralFlexion.class, "numeralBase",
            PronombreFlexion.class, "pronombreBase",
            ParticulaFlexion.class, "particulaBase"
    );

    /**
     * Traduce un {@link CriterioBusquedaNuevo} plano (sin dependencias) a una {@link Specification} JPA.
     * <p>
     * Las restricciones de igualdad se combinan con AND entre campos y OR entre valores del mismo campo.
     * Las restricciones numéricas se combinan con AND.
     *
     * @param criterio criterio plano a traducir
     * @param <T>      tipo de flexión
     * @return Specification equivalente al criterio
     */
    public <T extends PalabraFlexion<?>> Specification<T> toSpecification(CriterioBusquedaNuevo<T> criterio) {
        return (root, query, cb) -> {
            Predicate predicado = cb.conjunction(); // AND acumulativo

            // Restricciones de igualdad y exclusión
            for (Map.Entry<String, Set<Object>> entry : criterio.getRestricciones().entrySet()) {
                String campo = entry.getKey();
                Set<Object> valores = entry.getValue();

                if (campo.startsWith("!")) {
                    // Exclusión: el valor NO debe estar en el conjunto
                    String campoReal = campo.substring(1);
                    Path<?> path = resolverPath(root, campoReal, criterio.getTipoFlexion());
                    predicado = cb.and(predicado, path.in(valores).not());
                } else {
                    // Inclusión: el valor debe estar en el conjunto (OR entre valores)
                    Path<?> path = resolverPath(root, campo, criterio.getTipoFlexion());
                    predicado = cb.and(predicado, path.in(valores));
                }
            }

            // Restricciones numéricas
            for (RestriccionNumerica restriccion : criterio.getRestriccionesNumericas()) {
                @SuppressWarnings("unchecked")
                Path<Integer> path = (Path<Integer>) resolverPath(root, restriccion.campo(), criterio.getTipoFlexion());

                Predicate predicadoNumerico = switch (restriccion) {
                    case RestriccionNumerica.Valores v -> path.in(v.valores());
                    case RestriccionNumerica.Entre e -> cb.between(path, e.min(), e.max());
                    case RestriccionNumerica.MayorQue mq -> cb.greaterThan(path, mq.umbral());
                    case RestriccionNumerica.MenorQue mq -> cb.lessThan(path, mq.umbral());
                };

                predicado = cb.and(predicado, predicadoNumerico);
            }

            return predicado;
        };
    }

    /**
     * Resuelve la ruta JPA para un nombre de campo, manejando el prefijo {@code base.}
     * para navegar a la palabra base mediante join.
     *
     * @param root          raíz de la consulta
     * @param nombreCampo   nombre del campo (ej. {@code "caso"}, {@code "base.genero"})
     * @param tipoFlexion   clase de la flexión para determinar el nombre de la relación base
     * @return la ruta JPA al campo
     */
    private Path<?> resolverPath(Root<?> root, String nombreCampo,
                                  Class<? extends PalabraFlexion<?>> tipoFlexion) {
        if (nombreCampo.startsWith("base.")) {
            String campoBase = nombreCampo.substring(5);
            String nombreRelacion = NOMBRE_RELACION_BASE.get(tipoFlexion);
            if (nombreRelacion == null) {
                throw new IllegalArgumentException(
                        "Tipo de flexión no soportado para acceso a base: " + tipoFlexion.getSimpleName());
            }
            return root.join(nombreRelacion, JoinType.INNER).get(campoBase);
        }
        return root.get(nombreCampo);
    }
}

