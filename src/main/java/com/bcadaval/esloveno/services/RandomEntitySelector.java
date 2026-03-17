package com.bcadaval.esloveno.services;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Componente genérico para seleccionar entidades aleatorias de forma eficiente.
 * <p>
 * En lugar de traer todos los registros a memoria y elegir uno, realiza:
 * <ol>
 *   <li>{@code COUNT} con los filtros → obtiene el total de candidatos</li>
 *   <li>Genera un offset aleatorio entre 0 y count-1</li>
 *   <li>{@code SELECT} con {@code LIMIT 1 OFFSET offset} → obtiene un único registro</li>
 * </ol>
 * <p>
 * Esto es mucho más eficiente que {@code ORDER BY RANDOM()} o cargar toda la tabla.
 */
@Component
public class RandomEntitySelector {

    /**
     * Selecciona una entidad aleatoria que cumpla la especificación dada.
     *
     * @param repo repositorio que implementa {@link JpaSpecificationExecutor}
     * @param spec especificación con los filtros a aplicar
     * @param <T>  tipo de la entidad
     * @return la entidad seleccionada, o {@link Optional#empty()} si no hay candidatos
     */
    public <T> Optional<T> selectRandom(JpaSpecificationExecutor<T> repo, Specification<T> spec) {
        long count = repo.count(spec);
        if (count == 0) {
            return Optional.empty();
        }

        int offset = ThreadLocalRandom.current().nextInt((int) count);
        List<T> resultado = repo.findAll(spec, PageRequest.of(offset, 1)).getContent();

        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.getFirst());
    }

    /**
     * Selecciona una entidad aleatoria sin filtros adicionales, usando el repositorio completo.
     *
     * @param repo repositorio que implementa {@link JpaSpecificationExecutor}
     * @param <T>  tipo de la entidad
     * @return la entidad seleccionada, o {@link Optional#empty()} si la tabla está vacía
     */
    public <T> Optional<T> selectRandom(JpaSpecificationExecutor<T> repo) {
        return selectRandom(repo, Specification.where(null));
    }
}

