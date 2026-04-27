package com.bcadaval.esloveno.services;

import com.bcadaval.esloveno.beans.EstructuraFraseConfig;
import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;
import com.bcadaval.esloveno.beans.enums.CategoriaFrase;
import com.bcadaval.esloveno.repo.EstructuraFraseConfigRepo;
import com.bcadaval.esloveno.structures.frase.Frase;
import com.bcadaval.esloveno.structures.frase.criterio.CriterioBusquedaNuevo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar las {@link Frase} del nuevo sistema.
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Auto-registrar nuevas frases en BD</li>
 *   <li>Validar las frases al arranque (detección de ciclos, validaciones estructurales)</li>
 *   <li>Filtrar frases activas y válidas</li>
 *   <li>Extraer y cachear criterios de búsqueda expandidos por tipo de flexión</li>
 *   <li>Calcular criterios de palabras estudiables (al arranque y al guardar configuración)</li>
 * </ul>
 * <p>
 * Los criterios expandidos se almacenan en memoria y se recalculan cuando:
 * <ol>
 *   <li>La aplicación arranca</li>
 *   <li>El usuario guarda la configuración de frases activas</li>
 * </ol>
 */
@Log4j2
@Service
public class FraseService {

    @Autowired
    private List<Frase> todasLasFrases;

    @Autowired
    private EstructuraFraseConfigRepo repo;

    @Lazy
    @Autowired
    private InitializationService initializationService;

    @Lazy
    @Autowired
    private ElegibilidadService elegibilidadService;

    /**
     * Flag para controlar que solo se registran una vez.
     */
    private final AtomicBoolean frasesRegistradas = new AtomicBoolean(false);

    /**
     * Flag para controlar que la validación inicial solo se ejecuta una vez.
     */
    private final AtomicBoolean validacionEjecutada = new AtomicBoolean(false);

    /**
     * Caché de criterios expandidos por tipo de flexión.
     * Clave: clase de flexión, Valor: lista de criterios expandidos (OR entre ellos).
     * Se recalcula al arranque y al guardar configuración.
     */
    private volatile Map<Class<? extends PalabraFlexion<?>>, List<CriterioBusquedaNuevo<?>>> cacheCriterios = new HashMap<>();

    // ============================================
    // Inicialización al arranque
    // ============================================

    /**
     * Se ejecuta cuando la aplicación está completamente lista.
     * Registra las frases en BD, valida su estructura y calcula los criterios
     * de búsqueda para que estén disponibles desde la primera llamada a {@code /getWords}.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (initializationService.isDatabaseReady()) {
            log.info("Aplicación lista. Iniciando registro, validación y cálculo de criterios...");
            autoRegistrarFrasesIfNeeded();
            // Si autoRegistrarFrasesIfNeeded ya ejecutó la validación (primera vez), no repetir
            if (!validacionEjecutada.get()) {
                validarTodasLasFrases();
            }
        } else {
            log.warn("BD no lista al arranque. Los criterios se calcularán en la primera petición.");
        }
    }

    // ============================================
    // Auto-registro en BD
    // ============================================

    /**
     * Registra automáticamente las frases nuevas en BD como activas.
     */
    private void autoRegistrarFrasesIfNeeded() {
        if (!initializationService.isDatabaseReady() || !frasesRegistradas.compareAndSet(false, true)) {
            return;
        }

        try {
            Set<String> existentesEnBD = repo.findAll().stream()
                    .map(EstructuraFraseConfig::getIdentificador)
                    .collect(Collectors.toSet());

            List<EstructuraFraseConfig> nuevas = todasLasFrases.stream()
                    .filter(f -> !existentesEnBD.contains(f.getIdentificador()))
                    .map(f -> EstructuraFraseConfig.builder()
                            .identificador(f.getIdentificador())
                            .activa(true)
                            .build())
                    .toList();

            if (!nuevas.isEmpty()) {
                repo.saveAll(nuevas);
                log.info("Auto-registradas {} nuevas frases: {}",
                        nuevas.size(),
                        nuevas.stream().map(EstructuraFraseConfig::getIdentificador).toList());
            }

            // Validar frases y cachear criterios tras el primer registro
            if (validacionEjecutada.compareAndSet(false, true)) {
                validarTodasLasFrases();
            }
        } catch (Exception e) {
            log.warn("No se pudieron registrar frases (BD no lista): {}", e.getMessage());
            frasesRegistradas.set(false);
        }
    }

    // ============================================
    // Validación
    // ============================================

    /**
     * Valida todas las frases registradas. Debe llamarse al arranque de la aplicación.
     * <p>
     * Ejecuta la validación estructural de cada frase (ciclos, huecos sin criterio/generador).
     * Las frases inválidas se marcan internamente y no participan en el estudio.
     * <strong>No interrumpe el arranque</strong>; solo emite warnings.
     */
    public void validarTodasLasFrases() {
        autoRegistrarFrasesIfNeeded();
        for (Frase frase : todasLasFrases) {
            frase.validar();
            if (frase.isInvalida()) {
                log.warn("⚠️ Frase '{}' inválida: {}", frase.getNombreMostrar(), frase.getMotivoInvalidez());
            }
        }
        recalcularCriterios();
        log.info("Validación de frases completada. {} frases totales, {} válidas, {} inválidas",
                todasLasFrases.size(),
                todasLasFrases.stream().filter(f -> !f.isInvalida()).count(),
                todasLasFrases.stream().filter(Frase::isInvalida).count());
    }

    // ============================================
    // Consultas de frases
    // ============================================

    /**
     * Obtiene las frases activas en BD y válidas estructuralmente.
     *
     * @return lista de frases listas para participar en el estudio
     */
    public List<Frase> getFrasesActivasYValidas() {
        autoRegistrarFrasesIfNeeded();

        Set<String> activasEnBD = repo.findByActivaTrue().stream()
                .map(EstructuraFraseConfig::getIdentificador)
                .collect(Collectors.toSet());

        return todasLasFrases.stream()
                .filter(f -> !f.isInvalida())
                .filter(f -> activasEnBD.contains(f.getIdentificador()))
                .toList();
    }

    // ============================================
    // Criterios expandidos (caché)
    // ============================================

    /**
     * Recalcula y cachea los criterios expandidos de todas las frases activas y válidas.
     * <p>
     * Para cada frase activa, se recorren sus huecos con criterio y se expanden
     * todas las ramas de dependencias (producto cartesiano). Los criterios del mismo
     * tipo de flexión se agrupan (OR entre ellos).
     * <p>
     * Debe llamarse al arranque y cada vez que se guarda la configuración.
     */
    public void recalcularCriterios() {
        Map<Class<? extends PalabraFlexion<?>>, List<CriterioBusquedaNuevo<?>>> nuevaCache = new HashMap<>();

        for (Frase frase : getFrasesActivasYValidas()) {
            for (CriterioBusquedaNuevo<?> criterio : frase.getCriteriosBusqueda()) {
                List<? extends CriterioBusquedaNuevo<?>> expandidos = criterio.expandirDependencias();
                for (CriterioBusquedaNuevo<?> expandido : expandidos) {
                    nuevaCache.computeIfAbsent(expandido.getTipoFlexion(), k -> new ArrayList<>())
                            .add(expandido);
                }
            }
        }

        this.cacheCriterios = nuevaCache;

        log.info("Criterios recalculados. Tipos con criterios: {}",
                nuevaCache.entrySet().stream()
                        .map(e -> e.getKey().getSimpleName() + "(" + e.getValue().size() + ")")
                        .toList());

        // Recalcular elegibilidad en BD tras actualizar los criterios
        elegibilidadService.recalcularTodo();
    }

    /**
     * Obtiene los criterios expandidos cacheados para un tipo de flexión.
     *
     * @param tipoFlexion clase del tipo de flexión
     * @return lista de criterios (OR entre ellos), vacía si no hay criterios para ese tipo
     */
    @SuppressWarnings("unchecked")
    public <T extends PalabraFlexion<?>> List<CriterioBusquedaNuevo<T>> getCriteriosPorTipo(Class<T> tipoFlexion) {
        List<CriterioBusquedaNuevo<?>> criterios = cacheCriterios.getOrDefault(tipoFlexion, List.of());
        // Cast seguro: sabemos que los criterios del mapa son del tipo correcto
        return criterios.stream()
                .map(c -> (CriterioBusquedaNuevo<T>) c)
                .toList();
    }

    // ============================================
    // Configuración UI
    // ============================================

    /**
     * Obtiene todas las frases para la pantalla de configuración.
     * Incluye nombre, estado activo/inactivo, dificultad e indicador de invalidez.
     */
    public List<FraseConfigDTO> getTodasParaConfiguracion() {
        autoRegistrarFrasesIfNeeded();

        Map<String, Boolean> estadoBD = repo.findAll().stream()
                .collect(Collectors.toMap(
                        EstructuraFraseConfig::getIdentificador,
                        EstructuraFraseConfig::getActiva
                ));

        return todasLasFrases.stream()
                .map(f -> new FraseConfigDTO(
                        f.getIdentificador(),
                        f.getNombreMostrar(),
                        estadoBD.getOrDefault(f.getIdentificador(), true),
                        f.getCasosUsados(),
                        f.getFormasVerbalesUsadas(),
                        f.getCategoria(),
                        f.getDificultad(),
                        f.isInvalida(),
                        f.getMotivoInvalidez()
                ))
                .sorted(Comparator.comparingInt(dto -> dto.dificultad().getOrden()))
                .toList();
    }

    /**
     * Obtiene las frases agrupadas por nivel de dificultad y luego por categoría.
     */
    public Map<NivelDificultad, Map<CategoriaFrase, List<FraseConfigDTO>>> getFrasesAgrupadasPorNivelYCategoria() {
        return getTodasParaConfiguracion().stream()
                .collect(Collectors.groupingBy(
                        FraseConfigDTO::getDificultad,
                        () -> new TreeMap<>(Comparator.comparingInt(NivelDificultad::getOrden)),
                        Collectors.groupingBy(
                                FraseConfigDTO::getCategoria,
                                () -> new TreeMap<>(Comparator.comparingInt(CategoriaFrase::getOrden)),
                                Collectors.toList()
                        )
                ));
    }

    /**
     * Activa o desactiva una frase. Recalcula criterios tras el cambio.
     */
    public void setActiva(String identificador, boolean activa) {
        EstructuraFraseConfig config = repo.findById(identificador)
                .orElseThrow(() -> new IllegalArgumentException("Frase no encontrada: " + identificador));
        config.setActiva(activa);
        repo.save(config);
        log.info("Frase '{}' {}", identificador, activa ? "activada" : "desactivada");
    }

    /**
     * Recalcula criterios tras guardar la configuración.
     * Llamar después de todas las operaciones de toggle.
     */
    public void onConfiguracionGuardada() {
        recalcularCriterios();
    }

    /**
     * Obtiene los casos activos derivados de las frases activas.
     */
    public Set<Caso> getCasosActivos() {
        return getFrasesActivasYValidas().stream()
                .map(Frase::getCasosUsados)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Obtiene las formas verbales activas.
     */
    public Set<FormaVerbal> getFormasVerbalesActivas() {
        return getFrasesActivasYValidas().stream()
                .map(Frase::getFormasVerbalesUsadas)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    // ============================================
    // DTO
    // ============================================

    /**
     * DTO para la configuración de frases en la UI.
     * Incluye información de invalidez para mostrar frases en rojo con tooltip.
     * <p>
     * Los getters explícitos son necesarios para compatibilidad con JSP/EL.
     */
    @SuppressWarnings("unused")
    public record FraseConfigDTO(String identificador, String nombreMostrar, boolean activa,
                                 Set<Caso> casosUsados, Set<FormaVerbal> formasVerbalesUsadas,
                                 CategoriaFrase categoria, NivelDificultad dificultad,
                                 boolean invalida, String motivoInvalidez) {

        public String getIdentificador() { return identificador; }
        public String getNombreMostrar() { return nombreMostrar; }
        public boolean isActiva() { return activa; }
        public Set<Caso> getCasosUsados() { return casosUsados; }
        public Set<FormaVerbal> getFormasVerbalesUsadas() { return formasVerbalesUsadas; }
        public CategoriaFrase getCategoria() { return categoria; }
        public NivelDificultad getDificultad() { return dificultad; }
        public boolean isInvalida() { return invalida; }
        public String getMotivoInvalidez() { return motivoInvalidez; }
    }
}
