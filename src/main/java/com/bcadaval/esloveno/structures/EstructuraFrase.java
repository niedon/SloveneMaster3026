package com.bcadaval.esloveno.structures;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.bcadaval.esloveno.services.palabra.NumeralService;
import com.bcadaval.esloveno.services.palabra.PronombreService;
import com.bcadaval.esloveno.services.palabra.sustantivo.SustantivoService;
import com.bcadaval.esloveno.services.palabra.verbo.VerbosService;
import com.bcadaval.esloveno.structures.extractores.ExtraccionApoyoEstandar;
import com.bcadaval.esloveno.structures.extractores.ExtraccionSlotEstandar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;
import com.bcadaval.esloveno.beans.enums.NivelDificultad;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Clase abstracta base para estructuras de frase.
 * Define el patrón de elementos que deben cumplirse para formar una frase válida.
 * <p>
 * La frase puede contener:
 * - Slots: elementos con CriterioBusqueda, buscan palabras en repositorios (participan en SRS)
 * - Apoyos: elementos con generadorObjeto, generan palabras dinámicamente sin ID
 * - Opcionales: elementos con ambos (criterio + generador). Participan en la búsqueda por
 *   criterio pero no bloquean la completitud de la frase. Si no se rellenan por criterio,
 *   se usa el generador como fallback y se tratan como apoyo en el frontend.
 * <p>
 * El orden de los elementos en la lista determina el orden en la vista.
 * El orden en que se añaden en configurarEstructura es indiferente gracias a
 * la resolución de dependencias en tiempo de construcción.
 * <p>
 * Las implementaciones deben ser beans de Spring (@Component) para:
 * - Inyección automática de dependencias
 * - Auto-registro en base de datos
 * - Activación/desactivación dinámica
 * <p>
 * <strong>ADVERTENCIA DE CONCURRENCIA:</strong> Esta clase es un singleton de Spring
 * pero contiene estado mutable (palabraAsignada en ElementoFrase).
 * La función {@link #limpiar()} debe llamarse SIEMPRE antes de reutilizar la estructura.
 * En un entorno con múltiples usuarios concurrentes, considerar:
 * - Usar @Scope("prototype") en las implementaciones
 * - O crear copias de las estructuras antes de asignar palabras
 * - O sincronizar el acceso a nivel de controlador
 * Actualmente es seguro para uso monousuario.
 */
@Log4j2
@Getter
@Component
public abstract class EstructuraFrase {

    @Autowired
    protected PronombreService pronombreService;

    @Autowired
    protected NumeralService numeralService;

    @Autowired
    protected SustantivoService sustantivoService;

    @Autowired
    protected VerbosService verbosService;

    @Autowired
    protected ExtraccionSlotEstandar extraccionSlotEstandar;

    @Autowired
    protected ExtraccionApoyoEstandar extraccionApoyoEstandar;

    @Lazy
    @Autowired
    protected com.bcadaval.esloveno.services.RepeticionEspaciadaService repeticionEspaciadaService;

    /**
     * Lista ordenada de todos los elementos de la frase (slots + apoyos).
     * El orden determina cómo se muestra en la vista.
     */
    protected final List<ElementoFrase<? extends PalabraFlexion<?>>> elementos = new ArrayList<>();

    /**
     * Lista de slots (elementos con criterio de búsqueda) para búsqueda rápida.
     */
    protected final List<ElementoFrase<? extends PalabraFlexion<?>>> slots = new ArrayList<>();

    /**
     * Lista de apoyos (elementos con generador puro) para procesamiento posterior.
     */
    protected final List<ElementoFrase<? extends PalabraFlexion<?>>> apoyos = new ArrayList<>();

    /**
     * Lista de opcionales (elementos con criterio + generador).
     * Participan en búsqueda por criterio pero no bloquean la completitud.
     */
    protected final List<ElementoFrase<? extends PalabraFlexion<?>>> opcionales = new ArrayList<>();

    /**
     * Constructor por defecto para Spring
     */
    protected EstructuraFrase() {
    }

    /**
     * Añade un elemento a la estructura.
     * Clasifica automáticamente como slot, apoyo u opcional según su configuración.
     *
     * @param elemento Elemento a añadir
     */
    protected void agregarElemento(ElementoFrase<? extends PalabraFlexion<?>> elemento) {
        elementos.add(elemento);
        if (elemento.esOpcional()) {
            // Opcional: tiene criterio Y generador
            // Se añade a slots (para participar en búsqueda) y a opcionales (para tracking)
            slots.add(elemento);
            opcionales.add(elemento);
        } else if (elemento.esSlot()) {
            slots.add(elemento);
        } else if (elemento.esApoyo()) {
            apoyos.add(elemento);
        }
    }

    /**
     * Obtiene los criterios de búsqueda de todos los slots.
     * Útil para EstructuraPalabraService.
     *
     * @return Lista de criterios de búsqueda
     */
    public List<? extends CriterioBusqueda<?>> getCriteriosBusqueda() {
        return slots.stream()
                .map(e -> (CriterioBusqueda<?>) e.getCriterioBusqueda())
                .toList();
    }


    /**
     * Intenta asignar una palabra a algún slot vacío que coincida
     * @param palabra Palabra a intentar asignar
     * @return true si se asignó a algún slot, false si no coincide con ninguno
     */
    public boolean intentarAsignar(PalabraFlexion<?> palabra) {
        for (var slot : slots) {
            if (slot.coincide(palabra)) {
                slot.asignar(palabra);
                log.debug("Asignado {} a slot '{}'", palabra.getClass().getSimpleName(), slot.getNombre());
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si todos los slots obligatorios tienen una palabra asignada.
     * Los elementos opcionales (con criterio + generador) no bloquean la completitud.
     */
    public boolean estaCompleta() {
        return slots.stream()
                .filter(ElementoFrase::esSlotObligatorio)
                .allMatch(ElementoFrase::estaAsignado);
    }

    public Instant calcularMediaInstant() {
        return Instant.ofEpochMilli( (long) slots.stream()
                .filter(ElementoFrase::estaAsignado)
                .map(ElementoFrase::getPalabraAsignada)
                .filter(Objects::nonNull)
                .map(PalabraFlexion::getProximaRevision)
                .filter(Objects::nonNull)
                .mapToLong(Instant::toEpochMilli)
                .average()
                .orElse(Double.MIN_VALUE));
    }

    /**
     * Construye la lista de datos de visualización para el JSP.
     * El modo se decide ALEATORIAMENTE aquí (singleton con modo dinámico).
     * Itera sobre los elementos EN ORDEN para mantener la estructura de la frase.
     * El JSP recibirá textoFila1 y textoFila2 sin saber qué idioma es cada uno.
     * <p>
     * IMPORTANTE: Orden de generación:
     * 1. Opcionales no rellenados por criterio → se rellenan con generador (fallback)
     * 2. Apoyos → se generan (pueden depender de opcionales ya rellenados)
     * <p>
     * Si cualquier generador (opcional o apoyo) devuelve null, la frase se considera
     * inválida y se devuelve null. El controlador debe descartar esta estructura y
     * probar con la siguiente candidata.
     *
     * @return Lista de DatoVisualizacion, o null si algún generador falló
     */
    public List<DatoVisualizacion> construirDatosVisualizacion() {
        // Modo aleatorio cada vez que se construye
        ModoVisualizacion modo = ModoVisualizacion.aleatorio();
        log.debug("Construyendo datos con modo: {}", modo);

        // 1. Rellenar opcionales no asignados con generador (fallback)
        for (var opcional : opcionales) {
            if (!opcional.estaAsignado()) {
                if (!generarYAsignarOpcionalComoFallback(opcional)) {
                    log.error("FRASE DESCARTADA '{}': el opcional '{}' no pudo ser generado por fallback",
                            getNombreMostrar(), opcional.getNombre());
                    return null;
                }
            }
        }

        // 2. Generar apoyos puros (pueden depender de opcionales ya rellenados)
        for (var apoyo : apoyos) {
            if (!generarYAsignarApoyo(apoyo)) {
                log.error("FRASE DESCARTADA '{}': el apoyo '{}' no pudo ser generado",
                        getNombreMostrar(), apoyo.getNombre());
                return null;
            }
        }

        // Construir datos de visualización en orden
        return elementos.stream()
                .map(el -> construirDato(el, modo))
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Genera y asigna el objeto de apoyo. Devuelve false si el generador devuelve null.
     */
    private <T extends PalabraFlexion<?>> boolean generarYAsignarApoyo(ElementoFrase<T> apoyo) {
        T objetoGenerado = apoyo.generarObjeto(this);
        if (objetoGenerado == null) {
            return false;
        }
        apoyo.asignar(objetoGenerado);
        return true;
    }

    /**
     * Genera y asigna un opcional como fallback por generador.
     * Marca el elemento como rellenado por generador.
     * Devuelve false si el generador devuelve null.
     */
    private <T extends PalabraFlexion<?>> boolean generarYAsignarOpcionalComoFallback(ElementoFrase<T> opcional) {
        T objetoGenerado = opcional.generarObjeto(this);
        if (objetoGenerado == null) {
            return false;
        }
        opcional.asignarComoGenerado(objetoGenerado);
        log.debug("Opcional '{}' rellenado por generador (fallback)", opcional.getNombre());
        return true;
    }

    /**
     * Construye un DatoVisualizacion para un elemento.
     * Los elementos opcionales rellenados por generador se tratan como apoyo (sin SRS).
     */
    private DatoVisualizacion construirDato(ElementoFrase<?> elemento, ModoVisualizacion modo) {
        if (!elemento.estaAsignado()) return null;

        PalabraFlexion<?> palabra = elemento.getPalabraAsignada();

        // Determinar si este elemento participa en SRS:
        // - Slots obligatorios: siempre participan
        // - Opcionales rellenados por criterio: participan
        // - Opcionales rellenados por generador: NO participan (se tratan como apoyo)
        // - Apoyos: NO participan
        boolean participaEnSRS = elemento.esSlot() && !elemento.isFueRellenadoPorGenerador();

        // Calcular intervalos si participa en SRS
        String intervaloArriba = null;
        String intervaloAbajo = null;

        if (participaEnSRS && repeticionEspaciadaService != null) {
            try {
                long segundosArriba = repeticionEspaciadaService.calcularProximoIntervalo(palabra, true);
                long segundosAbajo = repeticionEspaciadaService.calcularProximoIntervalo(palabra, false);
                intervaloArriba = DatoVisualizacion.formatearIntervalo(segundosArriba);
                intervaloAbajo = DatoVisualizacion.formatearIntervalo(segundosAbajo);
            } catch (Exception e) {
                log.warn("Error calculando intervalos para {}: {}", palabra.getFlexion(), e.getMessage());
            }
        }

        return DatoVisualizacion.builder()
                .textoFila1(elemento.getTextoFila1(modo))
                .textoFila2(elemento.getTextoFila2(modo))
                .id(participaEnSRS ? palabra.getId() : null)
                .tipo(participaEnSRS ? FraseTipoPalabra.fromObject(palabra) : null)
                .intervaloArriba(intervaloArriba)
                .intervaloAbajo(intervaloAbajo)
                .build();
    }

    /**
     * Limpia todos los elementos para reutilización (singleton)
     */
    public void limpiar() {
        elementos.forEach(ElementoFrase::limpiar);
    }

    /**
     * Identificador único para BD y configuración.
     * Debe ser constante y único (ej: "VERBO_TRANSITIVO_ACUSATIVO")
     */
    public abstract String getIdentificador();

    /**
     * Nombre para mostrar en la UI de configuración.
     * (ej: "Verbo Transitivo + Complemento Directo")
     */
    public abstract String getNombreMostrar();

    /**
     * Nombre descriptivo de la estructura para logging (puede ser igual a getNombreMostrar)
     */
    public String getNombre() {
        return getNombreMostrar();
    }

    /**
     * Conjunto de casos gramaticales que usa esta estructura.
     * Se calcula dinámicamente a partir de los criterios de los slots.
     * Extrae los valores de CASO de los CriterioGramatical.
     */
    public Set<Caso> getCasosUsados() {
        Set<Caso> casos = new HashSet<>();
        for (ElementoFrase<?> slot : slots) {
            CriterioBusqueda<?> criterio = slot.getCriterioBusqueda();
            if (criterio == null || criterio.getCriterioGramatical() == null) continue;

            Object valorCaso = criterio.getCriterioGramatical()
                    .getRequisitos()
                    .get(CaracteristicaGramatical.CASO);

            if (valorCaso instanceof Caso caso) {
                casos.add(caso);
            }
        }
        return casos;
    }

    /**
     * Conjunto de formas verbales que usa esta estructura.
     * Se calcula dinámicamente a partir de los criterios de los slots.
     * Extrae los valores de FORMA_VERBAL de los CriterioGramatical.
     */
    public Set<FormaVerbal> getFormasVerbalesUsadas() {
        return slots.stream()
                .map(ElementoFrase::getCriterioBusqueda)
                .filter(Objects::nonNull)
                .filter(criterio -> criterio.getCriterioGramatical() != null)
                .map(criterio -> criterio.getCriterioGramatical().getRequisitos().get(CaracteristicaGramatical.FORMA_VERBAL))
                .filter(valor -> valor instanceof FormaVerbal)
                .map(valor -> (FormaVerbal) valor)
                .collect(Collectors.toSet());
    }

    /**
     * Obtiene el nivel de dificultad de esta estructura.
     * Si no tiene la anotación @DificultadFrase, devuelve PRINCIPIANTE por defecto.
     */
    public NivelDificultad getDificultad() {
        DificultadFrase anotacion = this.getClass().getAnnotation(DificultadFrase.class);
        if(anotacion == null) {
            log.warn("La estructura '{}' no tiene anotación @DificultadFrase, asignando dificultad por defecto PRINCIPIANTE", getNombre());
        }
        return anotacion != null ? anotacion.value() : NivelDificultad.PRINCIPIANTE;
    }


}
