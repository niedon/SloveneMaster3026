package com.bcadaval.esloveno.services;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.bcadaval.esloveno.repo.AdjetivoFlexionRepo;
import com.bcadaval.esloveno.repo.SustantivoFlexionRepo;
import com.bcadaval.esloveno.repo.VerboFlexionRepo;
import com.bcadaval.esloveno.structures.specifications.AdjetivoFlexionSpecs;
import com.bcadaval.esloveno.structures.specifications.SustantivoFlexionSpecs;
import com.bcadaval.esloveno.structures.specifications.VerboFlexionSpecs;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.bcadaval.esloveno.beans.EstructuraFraseConfig;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.repo.EstructuraFraseConfigRepo;
import com.bcadaval.esloveno.structures.EstructuraFrase;

import lombok.extern.log4j.Log4j2;

/**
 * Servicio para gestionar las estructuras de frase.
 * - Auto-registra nuevas estructuras en BD
 * - Filtra estructuras activas
 * - Calcula casos activos derivados de las estructuras activas
 */
@Log4j2
@Service
public class EstructuraFraseService {

    @Autowired
    private List<EstructuraFrase> todasLasEstructuras;

    @Autowired
    private EstructuraFraseConfigRepo repo;

    @Lazy
    @Autowired
    private InitializationService initializationService;

    @Autowired
    private SustantivoFlexionRepo sustantivoFlexionRepo;

    @Autowired
    private VerboFlexionRepo verboFlexionRepo;

    @Autowired
    private AdjetivoFlexionRepo adjetivoFlexionRepo;

    /** Flag para controlar que solo se registran una vez */
    private final AtomicBoolean estructurasRegistradas = new AtomicBoolean(false);

    /**
     * Registra automáticamente las estructuras nuevas en BD como activas.
     * Se ejecuta de forma lazy cuando la BD está disponible.
     */
    private void autoRegistrarEstructurasIfNeeded() {
        // Solo ejecutar si la BD está lista y no se ha hecho antes
        if (!initializationService.isDatabaseReady() || !estructurasRegistradas.compareAndSet(false, true)) {
            return;
        }

        try {
            Set<String> existentesEnBD = repo.findAll().stream()
                .map(EstructuraFraseConfig::getIdentificador)
                .collect(Collectors.toSet());

            List<EstructuraFraseConfig> nuevas = todasLasEstructuras.stream()
                .filter(e -> !existentesEnBD.contains(e.getIdentificador()))
                .map(e -> EstructuraFraseConfig.builder()
                    .identificador(e.getIdentificador())
                    .activa(true)  // Por defecto activa
                    .build())
                .toList();

            if (!nuevas.isEmpty()) {
                repo.saveAll(nuevas);
                log.info("Auto-registradas {} nuevas estructuras de frase: {}",
                    nuevas.size(),
                    nuevas.stream().map(EstructuraFraseConfig::getIdentificador).toList());
            }
        } catch (Exception e) {
            log.warn("No se pudieron registrar estructuras (BD no lista): {}", e.getMessage());
            estructurasRegistradas.set(false); // Permitir reintentar
        }
    }

    /**
     * Obtiene solo las estructuras activas.
     */
    public List<EstructuraFrase> getEstructurasActivas() {
        autoRegistrarEstructurasIfNeeded();

        Set<String> activasEnBD = repo.findByActivaTrue().stream()
            .map(EstructuraFraseConfig::getIdentificador)
            .collect(Collectors.toSet());

        return todasLasEstructuras.stream()
            .filter(e -> activasEnBD.contains(e.getIdentificador()))
            .toList();
    }

    /**
     * Obtiene los casos activos derivados de las frases activas.
     * Un caso está activo si existen palabras disponibles con ese caso
     * según los criterios de las estructuras activas.
     */
    public Set<Caso> getCasosActivos() {
        Set<Caso> casosActivos = new HashSet<>();

        // Obtener sustantivos que cumplan criterios de estructuras activas
        Specification<SustantivoFlexion> specSustantivo = getSpecificationCombinadaPorTipo(SustantivoFlexion.class);
        if (specSustantivo != null) {
            // Añadir specification de palabra completa
            Specification<SustantivoFlexion> specCompleta = specSustantivo
                    .and(SustantivoFlexionSpecs.completaParaEstudio());

            List<SustantivoFlexion> sustantivos = sustantivoFlexionRepo.findAll(specCompleta);
            sustantivos.stream()
                    .map(SustantivoFlexion::getCaso)
                    .filter(Objects::nonNull)
                    .forEach(casosActivos::add);
        }

        // Obtener adjetivos que cumplan criterios de estructuras activas
        Specification<AdjetivoFlexion> specAdjetivo = getSpecificationCombinadaPorTipo(AdjetivoFlexion.class);
        if (specAdjetivo != null) {
            Specification<AdjetivoFlexion> specCompleta = specAdjetivo
                    .and(AdjetivoFlexionSpecs.completaParaEstudio());

            List<AdjetivoFlexion> adjetivos = adjetivoFlexionRepo.findAll(specCompleta);
            adjetivos.stream()
                    .map(AdjetivoFlexion::getCaso)
                    .filter(Objects::nonNull)
                    .forEach(casosActivos::add);
        }

        return casosActivos;
    }

    /**
     * Obtiene las formas verbales activas derivadas de las frases activas.
     * Una forma verbal está activa si existen verbos disponibles con esa forma
     * según los criterios de las estructuras activas.
     */
    public Set<FormaVerbal> getFormasVerbalesActivas() {
        Set<FormaVerbal> formasActivas = new HashSet<>();

        // Obtener verbos que cumplan criterios de estructuras activas
        Specification<VerboFlexion> specVerbo = getSpecificationCombinadaPorTipo(VerboFlexion.class);
        if (specVerbo != null) {
            // Añadir specification de palabra completa
            Specification<VerboFlexion> specCompleta = specVerbo
                    .and(VerboFlexionSpecs.completaParaEstudio());

            List<VerboFlexion> verbos = verboFlexionRepo.findAll(specCompleta);
            verbos.stream()
                    .map(VerboFlexion::getFormaVerbal)
                    .filter(Objects::nonNull)
                    .forEach(formasActivas::add);
        }

        return formasActivas;
    }

    /**
     * Obtiene la Specification combinada de todas las estructuras activas para un tipo de flexión.
     * Combina todos los criterios de todas las estructuras activas con OR.
     * <p>
     * Ejemplo: Si hay 2 estructuras activas:
     * - FraseSoloSustantivoNominativo: sustantivo con caso=NOMINATIVO
     * - FraseVerboTransitivoAcusativo: sustantivo con caso=ACUSATIVO
     * <p>
     * El resultado sería: (caso=NOMINATIVO) OR (caso=ACUSATIVO)
     *
     * @param tipoFlexion Clase del tipo de flexión (ej: SustantivoFlexion.class)
     * @return Specification combinada, o null si no hay criterios para ese tipo
     */
    public <T extends PalabraFlexion<?>> Specification<T> getSpecificationCombinadaPorTipo(Class<T> tipoFlexion) {
        List<Specification<T>> specs = getEstructurasActivas().stream()
                .map(e -> e.<T>getSpecificationPorTipo(tipoFlexion))
                .filter(Objects::nonNull)
                .toList();

        if (specs.isEmpty()) {
            return null;
        }

        return specs.stream()
                .reduce(Specification::or)
                .orElse(null);
    }

    /**
     * Verifica si hay alguna estructura activa que use el tipo de flexión dado.
     *
     * @param tipoFlexion Clase del tipo de flexión
     * @return true si al menos una estructura activa usa ese tipo
     */
    public <T extends PalabraFlexion<?>> boolean tieneEstructurasParaTipo(Class<T> tipoFlexion) {
        return getSpecificationCombinadaPorTipo(tipoFlexion) != null;
    }

    /**
     * Obtiene todas las estructuras para la pantalla de configuración.
     * Incluye el nombre para mostrar y el estado activo/inactivo.
     */
    public List<EstructuraFraseConfigDTO> getTodasParaConfiguracion() {
        autoRegistrarEstructurasIfNeeded();

        Map<String, Boolean> estadoBD = repo.findAll().stream()
            .collect(Collectors.toMap(
                EstructuraFraseConfig::getIdentificador,
                EstructuraFraseConfig::getActiva
            ));

        return todasLasEstructuras.stream()
            .map(e -> new EstructuraFraseConfigDTO(
                e.getIdentificador(),
                e.getNombreMostrar(),
                estadoBD.getOrDefault(e.getIdentificador(), true),
                e.getCasosUsados(),
                e.getFormasVerbalesUsadas()
            ))
            .toList();
    }

    /**
     * Activa o desactiva una estructura.
     */
    public void setActiva(String identificador, boolean activa) {
        EstructuraFraseConfig config = repo.findById(identificador)
            .orElseThrow(() -> new IllegalArgumentException("Estructura no encontrada: " + identificador));

        config.setActiva(activa);
        repo.save(config);

        log.info("Estructura '{}' {} ", identificador, activa ? "activada" : "desactivada");
    }

    /**
     * DTO para la configuración de estructuras
     */
    @Getter
    public static class EstructuraFraseConfigDTO {
        private final String identificador;
        private final String nombreMostrar;
        private final Boolean activa;
        private final Set<Caso> casosUsados;
        private final Set<FormaVerbal> formasVerbalesUsadas;

        public EstructuraFraseConfigDTO(String identificador, String nombreMostrar,
                                        Boolean activa, Set<Caso> casosUsados,
                                        Set<FormaVerbal> formasVerbalesUsadas) {
            this.identificador = identificador;
            this.nombreMostrar = nombreMostrar;
            this.activa = activa;
            this.casosUsados = casosUsados;
            this.formasVerbalesUsadas = formasVerbalesUsadas;
        }

    }
}

