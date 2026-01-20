package com.bcadaval.esloveno.structures;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.bcadaval.esloveno.services.palabra.NumeralService;
import com.bcadaval.esloveno.services.palabra.PronombreService;
import com.bcadaval.esloveno.services.palabra.sustantivo.SustantivoService;
import com.bcadaval.esloveno.structures.extractores.ExtraccionApoyoEstandar;
import com.bcadaval.esloveno.structures.extractores.ExtraccionSlotEstandar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.CaracteristicaGramatical;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Clase abstracta base para estructuras de frase.
 * Define el patrón de elementos que deben cumplirse para formar una frase válida.
 * <p>
 * La frase puede contener:
 * - Slots: elementos con CriterioBusqueda, buscan palabras en repositorios (participan en SRS)
 * - Apoyos: elementos con generadorObjeto, generan palabras dinámicamente sin ID
 * <p>
 * El orden de los elementos en la lista determina el orden en la vista.
 * El orden en que se añaden en configurarEstructura es indiferente gracias a
 * la resolución de dependencias en tiempo de construcción.
 * <p>
 * Las implementaciones deben ser beans de Spring (@Component) para:
 * - Inyección automática de dependencias
 * - Auto-registro en base de datos
 * - Activación/desactivación dinámica
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
    protected ExtraccionSlotEstandar extraccionSlotEstandar;

    @Autowired
    protected ExtraccionApoyoEstandar extraccionApoyoEstandar;

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
     * Lista de apoyos (elementos con generador) para procesamiento posterior.
     */
    protected final List<ElementoFrase<? extends PalabraFlexion<?>>> apoyos = new ArrayList<>();

    /**
     * Constructor por defecto para Spring
     */
    protected EstructuraFrase() {
    }

    /**
     * Añade un elemento a la estructura.
     * Clasifica automáticamente como slot o apoyo según su configuración.
     *
     * @param elemento Elemento a añadir (slot o apoyo)
     */
    protected void agregarElemento(ElementoFrase<? extends PalabraFlexion<?>> elemento) {
        elementos.add(elemento);
        if (elemento.esSlot()) {
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
     * Verifica si todos los slots tienen una palabra asignada
     */
    public boolean estaCompleta() {
        return slots.stream().allMatch(ElementoFrase::estaAsignado);
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
     * IMPORTANTE: Primero genera los objetos de apoyo (que dependen de slots asignados).
     */
    public List<DatoVisualizacion> construirDatosVisualizacion() {
        // Modo aleatorio cada vez que se construye
        ModoVisualizacion modo = ModoVisualizacion.aleatorio();
        log.debug("Construyendo datos con modo: {}", modo);

        // Primero generar y asignar objetos de apoyo
        apoyos.stream()
                .filter(ElementoFrase::esApoyo)
                .forEach(this::generarYAsignarApoyo);

        // Construir datos de visualización en orden
        return elementos.stream()
                .map(el -> construirDato(el, modo))
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Helper para capturar el tipo genérico y asignar apoyo correctamente.
     * Resuelve el problema de type capture en el bucle for-each.
     */
    private <T extends PalabraFlexion<?>> void generarYAsignarApoyo(ElementoFrase<T> apoyo) {
        T objetoGenerado = apoyo.generarObjeto(this);
        apoyo.asignar(objetoGenerado);
    }

    /**
     * Construye un DatoVisualizacion para un elemento
     */
    private DatoVisualizacion construirDato(ElementoFrase<?> elemento, ModoVisualizacion modo) {
        if (!elemento.estaAsignado()) return null;

        PalabraFlexion<?> palabra = elemento.getPalabraAsignada();

        return DatoVisualizacion.builder()
                .textoFila1(elemento.getTextoFila1(modo))
                .textoFila2(elemento.getTextoFila2(modo))
                .id(elemento.esSlot() ? palabra.getId() : null)
                .tipo(elemento.esSlot() ? FraseTipoPalabra.fromObject(palabra) : null)
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


}
