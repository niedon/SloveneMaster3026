package com.bcadaval.esloveno.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
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

    /**
     * Lista ordenada de todos los elementos de la frase (slots + apoyos).
     * El orden determina cómo se muestra en la vista.
     */
    protected final List<ElementoFrase<?>> elementos = new ArrayList<>();

    /**
     * Lista de slots (elementos con criterio de búsqueda) para búsqueda rápida.
     */
    protected final List<ElementoFrase<?>> slots = new ArrayList<>();

    /**
     * Lista de apoyos (elementos con generador) para procesamiento posterior.
     */
    protected final List<ElementoFrase<?>> apoyos = new ArrayList<>();

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
    protected void agregarElemento(ElementoFrase<?> elemento) {
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
     * Obtiene la Specification combinada para un tipo específico de flexión.
     * Combina todos los criterios del mismo tipo con OR.
     *
     * @param tipoFlexion Clase del tipo de flexión
     * @return Specification combinada o null si no hay criterios de ese tipo
     */
    @SuppressWarnings("unchecked")
    public <T extends PalabraFlexion<?>> Specification<T> getSpecificationPorTipo(Class<T> tipoFlexion) {
        List<Specification<T>> specs = slots.stream()
                .map(ElementoFrase::getCriterioBusqueda)
                .filter(c -> c.getTipoFlexion().equals(tipoFlexion))
                .map(c -> (Specification<T>) c.getSpecification())
                .toList();

        if (specs.isEmpty()) return null;

        return specs.stream()
                .reduce(Specification::or)
                .orElse(null);
    }

    /**
     * Intenta asignar una palabra a algún slot vacío que coincida
     * @param palabra Palabra a intentar asignar
     * @param indice Índice de la palabra en la lista original
     * @return true si se asignó a algún slot, false si no coincide con ninguno
     */
    public boolean intentarAsignar(PalabraFlexion<?> palabra, int indice) {
        for (ElementoFrase<?> slot : slots) {
            if (slot.coincide(palabra)) {
                slot.asignar(palabra, indice);
                log.debug("Asignado {} a slot '{}' en índice {}",
                        palabra.getClass().getSimpleName(), slot.getNombre(), indice);
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

    /**
     * Obtiene los índices de las palabras usadas en esta estructura
     */
    public List<Integer> getIndicesUsados() {
        return slots.stream()
                .filter(ElementoFrase::estaAsignado)
                .map(ElementoFrase::getIndiceEnLista)
                .toList();
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
        for (ElementoFrase<?> apoyo : apoyos) {
            if (apoyo.esApoyo()) {
                PalabraFlexion objetoGenerado = apoyo.generarObjeto(this);
                apoyo.asignar(objetoGenerado, null);
            }
        }

        // Construir datos de visualización en orden
        List<DatoVisualizacion> datos = new ArrayList<>();
        for (ElementoFrase<?> elemento : elementos) {
            DatoVisualizacion dato = construirDato(elemento, modo);
            if (dato != null) {
                datos.add(dato);
            }
        }

        return datos;
    }

    /**
     * Construye un DatoVisualizacion para un elemento
     */
    private DatoVisualizacion construirDato(ElementoFrase<?> elemento, ModoVisualizacion modo) {
        if (!elemento.estaAsignado()) return null;

        PalabraFlexion palabra = elemento.getPalabraAsignada();

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
     * Se usa para calcular los casos activos derivados de las frases activas.
     */
    public abstract Set<Caso> getCasosUsados();

    /**
     * Conjunto de formas verbales que usa esta estructura.
     * Se usa para calcular las formas verbales activas derivadas de las frases activas.
     */
    public abstract Set<FormaVerbal> getFormasVerbalesUsadas();
}
