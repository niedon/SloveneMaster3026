package com.bcadaval.esloveno.services;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.structures.CriterioBusqueda;
import com.bcadaval.esloveno.structures.CriterioGramatical;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
     * Un caso está activo si está configurado en alguna estructura activa.
     * Usa el nuevo sistema de CriterioGramatical.
     */
    public Set<Caso> getCasosActivos() {
        return getEstructurasActivas().stream()
                .map(EstructuraFrase::getCasosUsados)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Obtiene las formas verbales activas derivadas de las frases activas.
     * Una forma verbal está activa si está configurada en alguna estructura activa.
     * Usa el nuevo sistema de CriterioGramatical.
     */
    public Set<FormaVerbal> getFormasVerbalesActivas() {
        return getEstructurasActivas().stream()
                .map(EstructuraFrase::getFormasVerbalesUsadas)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Obtiene los CriterioGramatical combinados de todas las estructuras activas para un tipo de flexión.
     * Útil para filtrar palabras en memoria.
     *
     * @param tipoFlexion Clase del tipo de flexión (ej: SustantivoFlexion.class)
     * @return Lista de CriterioGramatical, vacía si no hay criterios para ese tipo
     */
    public List<CriterioGramatical> getCriteriosGramaticalesPorTipo(Class<? extends PalabraFlexion<?>> tipoFlexion) {
        return getEstructurasActivas().stream()
                .flatMap(e -> e.getCriteriosBusqueda().stream())
                .filter(c -> c.getTipoFlexion().equals(tipoFlexion))
                .map(CriterioBusqueda::getCriterioGramatical)
                .filter(Objects::nonNull)
                .toList();
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
    public record EstructuraFraseConfigDTO(String identificador, String nombreMostrar, Boolean activa,
                                           Set<Caso> casosUsados, Set<FormaVerbal> formasVerbalesUsadas) {

    }
}

