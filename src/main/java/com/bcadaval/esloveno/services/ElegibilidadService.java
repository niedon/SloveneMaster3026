package com.bcadaval.esloveno.services;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.palabra.*;
import com.bcadaval.esloveno.repo.*;
import com.bcadaval.esloveno.structures.frase.criterio.CriterioBusquedaNuevo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio responsable de calcular y actualizar el campo {@code elegible} de las flexiones.
 * <p>
 * Una flexión es elegible si:
 * <ol>
 *   <li>Su palabra base está <strong>completa</strong> (según las reglas de completitud de cada tipo)</li>
 *   <li>Cumple al menos uno de los <strong>criterios expandidos</strong> de las frases activas</li>
 * </ol>
 * <p>
 * El recálculo se ejecuta en tres momentos:
 * <ul>
 *   <li>Al arrancar la aplicación (via {@link FraseService#onApplicationReady})</li>
 *   <li>Al guardar la configuración de frases (activar/desactivar)</li>
 *   <li>Al completar una palabra desde {@link com.bcadaval.esloveno.rest.CompletarPalabrasController}</li>
 * </ul>
 */
@Log4j2
@Service
public class ElegibilidadService {

    @Lazy
    @Autowired
    private FraseService fraseService;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private CriterioToSpecificationConverter converter;

    @Autowired
    private VerboFlexionRepo verboFlexionRepo;

    @Autowired
    private SustantivoFlexionRepo sustantivoFlexionRepo;

    @Autowired
    private AdjetivoFlexionRepo adjetivoFlexionRepo;

    @Autowired
    private NumeralFlexionRepo numeralFlexionRepo;

    @Autowired
    private PronombreFlexionRepo pronombreFlexionRepo;

    @Autowired
    private ParticulaFlexionRepo particulaFlexionRepo;

    /**
     * Recalcula la elegibilidad de TODAS las flexiones de TODOS los tipos.
     * <p>
     * Estrategia: para cada tipo, resetea todas las flexiones a {@code elegible=false}
     * y luego marca como {@code elegible=true} las que cumplen completitud + algún criterio.
     */
    @Transactional
    public void recalcularTodo() {
        log.info("Iniciando recálculo completo de elegibilidad...");

        recalcularTipo(
                VerboFlexion.class,
                verboFlexionRepo,
                FlexionSpecs.verboBaseCompleto()
        );

        recalcularTipo(
                SustantivoFlexion.class,
                sustantivoFlexionRepo,
                FlexionSpecs.sustantivoBaseCompleto()
        );

        recalcularTipo(
                AdjetivoFlexion.class,
                adjetivoFlexionRepo,
                FlexionSpecs.adjetivoBaseCompleto()
        );

        recalcularTipo(
                NumeralFlexion.class,
                numeralFlexionRepo,
                FlexionSpecs.numeralBaseCompleto()
        );

        recalcularTipo(
                PronombreFlexion.class,
                pronombreFlexionRepo,
                FlexionSpecs.pronombreCompleto()
        );

        recalcularTipo(
                ParticulaFlexion.class,
                particulaFlexionRepo,
                FlexionSpecs.particulaBaseCompleta()
        );

        log.info("Recálculo completo de elegibilidad finalizado.");
    }

    /**
     * Recalcula la elegibilidad para un tipo concreto de flexión.
     * <p>
     * Pasos:
     * <ol>
     *   <li>Obtener criterios expandidos del caché de {@link FraseService}</li>
     *   <li>Resetear todas las flexiones del tipo a {@code elegible=false}</li>
     *   <li>Buscar las que cumplen (baseCompleta AND algúnCriterio) y marcarlas {@code elegible=true}</li>
     *   <li>Guardar los cambios</li>
     * </ol>
     *
     * @param tipoFlexion    clase del tipo de flexión
     * @param jpaRepo        repositorio JPA (para findAll/saveAll)
     * @param specCompleta   Specification que filtra por palabra base completa
     * @param <T>            tipo de flexión
     */
    private <T extends PalabraFlexion<?>> void recalcularTipo(
            Class<T> tipoFlexion,
            FlexionBaseRepo<T, Integer> jpaRepo,
            Specification<T> specCompleta) {

        List<CriterioBusquedaNuevo<T>> criterios = fraseService.getCriteriosPorTipo(tipoFlexion);

        // Paso 1: Resetear todas a no elegible (Operación masiva en BD)
        jpaRepo.resetElegibilidad();

        if (criterios.isEmpty()) {
            // Sin criterios activos → ninguna elegible
            log.info("{}: 0 elegibles (sin criterios activos)", tipoFlexion.getSimpleName());
            return;
        }

        // Paso 2: Iterar por criterio
        for (CriterioBusquedaNuevo<T> criterio : criterios) {
            // Combinar: (Criterio X) AND (Completa)
            Specification<T> specCriterio = converter.toSpecification(criterio);
            Specification<T> specFinal = specCompleta.and(specCriterio);

            // Update directo en BD usando Subquery
            marcarElegiblesPorSpec(tipoFlexion, specFinal);
        }

        // Contamos los elegibles
        long totalElegibles = jpaRepo.count((root, query, cb) -> cb.isTrue(root.get("elegible")));

        log.info("{}: recalculo finalizado ({} elegibles totales)", tipoFlexion.getSimpleName(), totalElegibles);
    }

    /**
     * Ejecuta un UPDATE masivo marcando como elegibles las flexiones que cumplan la Specification.
     * Utiliza una subquery para resolver los IDs, evitando JOINS no soportados en UPDATE directo
     * y evitando cargar entidades en memoria.
     */
    private <T extends PalabraFlexion<?>> void marcarElegiblesPorSpec(Class<T> claseFlexion, Specification<T> spec) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // UPDATE Entity e SET e.elegible = true
        CriteriaUpdate<T> update = cb.createCriteriaUpdate(claseFlexion);
        Root<T> rootUpdate = update.from(claseFlexion);
        update.set("elegible", true);

        // Subquery: SELECT id FROM Entity WHERE spec...
        Subquery<Integer> subquery = update.subquery(Integer.class);
        Root<T> rootSub = subquery.from(claseFlexion);
        subquery.select(rootSub.get("id"));

        // Convertimos la spec a predicado usando el root de la subquery
        // Pasamos null en 'query' porque las specs actuales no lo usan, solo usan root y cb
        Predicate predicate = spec.toPredicate(rootSub, null, cb);
        subquery.where(predicate);

        // WHERE e.id IN (subquery)
        update.where(rootUpdate.get("id").in(subquery));

        em.createQuery(update).executeUpdate();
    }

    /**
     * Recalcula la elegibilidad para todas las flexiones de una palabra base concreta.
     * Se usa al completar los datos de una palabra desde {@link com.bcadaval.esloveno.rest.CompletarPalabrasController}.
     *
     * @param sloleksId identificador de la palabra base
     * @param tipoPalabra tipo de palabra (para saber qué repositorio y criterios usar)
     */
    @Transactional
    public void recalcularParaPalabra(String sloleksId, com.bcadaval.esloveno.beans.enums.TipoPalabra tipoPalabra) {
        switch (tipoPalabra) {
            case VERBO -> recalcularFlexionesDePalabra(
                    VerboFlexion.class,
                    verboFlexionRepo.findBySloleksId(sloleksId),
                    verboFlexionRepo);
            case SUSTANTIVO -> recalcularFlexionesDePalabra(
                    SustantivoFlexion.class,
                    sustantivoFlexionRepo.findBySloleksId(sloleksId),
                    sustantivoFlexionRepo);
            case ADJETIVO -> recalcularFlexionesDePalabra(
                    AdjetivoFlexion.class,
                    adjetivoFlexionRepo.findBySloleksId(sloleksId),
                    adjetivoFlexionRepo);
            case NUMERAL -> recalcularFlexionesDePalabra(
                    NumeralFlexion.class,
                    numeralFlexionRepo.findBySloleksId(sloleksId),
                    numeralFlexionRepo);
            case PRONOMBRE -> {
                // Pronombres: el significado está en la flexión, se pasa por ID
                // Para pronombres, sloleksId referencia al grupo; recalcular todas las flexiones del grupo
                List<PronombreFlexion> flexiones = pronombreFlexionRepo.findBySloleksId(sloleksId);
                recalcularFlexionesDePalabra(PronombreFlexion.class, flexiones, pronombreFlexionRepo);
            }
            case PARTICULA -> recalcularFlexionesDePalabra(
                    ParticulaFlexion.class,
                    particulaFlexionRepo.findBySloleksId(sloleksId),
                    particulaFlexionRepo);
        }
    }

    /**
     * Recalcula la elegibilidad para una lista concreta de flexiones usando los criterios
     * del caché de {@link FraseService} y evaluando en memoria con {@code cumpleCriteriosFijos}.
     * <p>
     * Este enfoque es eficiente para pocas flexiones (una sola palabra base).
     *
     * @param tipoFlexion clase del tipo de flexión
     * @param flexiones   lista de flexiones a evaluar
     * @param jpaRepo     repositorio para guardar
     * @param <T>         tipo de flexión
     */
    private <T extends PalabraFlexion<?>> void recalcularFlexionesDePalabra(
            Class<T> tipoFlexion,
            List<T> flexiones,
            JpaRepository<T, Integer> jpaRepo) {

        List<CriterioBusquedaNuevo<T>> criterios = fraseService.getCriteriosPorTipo(tipoFlexion);

        for (T flexion : flexiones) {
            boolean esElegible = !criterios.isEmpty()
                    && criterios.stream().anyMatch(c -> c.cumpleCriteriosFijos(flexion));
            flexion.setElegible(esElegible);
        }

        jpaRepo.saveAll(flexiones);
        long elegibles = flexiones.stream().filter(f -> Boolean.TRUE.equals(f.getElegible())).count();
        log.debug("{}: {} de {} flexiones marcadas como elegibles",
                tipoFlexion.getSimpleName(), elegibles, flexiones.size());
    }
}

