package com.bcadaval.esloveno.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import com.bcadaval.esloveno.beans.enums.Caso;
import com.bcadaval.esloveno.beans.enums.FormaVerbal;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Clase abstracta base para estructuras de frase.
 * Define el patrón de slots que deben cumplirse para formar una frase válida.
 *
 * La frase puede contener:
 * - Slots principales: requieren PalabraFlexion de la lista de tarjetas (con ID para SRS)
 * - Elementos de apoyo: generados dinámicamente (pronombres, números, etc.) sin ID
 *
 * El orden de los elementos en la lista determina el orden en la vista.
 *
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
     * Lista ordenada de elementos de la frase.
     * Cada elemento puede ser un SlotPalabra o un ElementoApoyo.
     * El orden determina cómo se muestra en la vista.
     */
    protected final List<Object> elementos = new ArrayList<>();

    /**
     * Lista de slots principales (para búsqueda rápida y validación)
     */
    protected final List<SlotPalabra> slots = new ArrayList<>();

    /**
     * Constructor por defecto para Spring
     */
    protected EstructuraFrase() {
    }

    /**
     * Añade un slot principal a la estructura.
     * El slot se añade tanto a la lista de slots como a la lista de elementos.
     */
    protected void agregarSlot(SlotPalabra slot) {
        slots.add(slot);
        elementos.add(slot);
    }

    /**
     * Añade un elemento de apoyo a la estructura.
     * Los elementos de apoyo no participan en el SRS (sin ID).
     */
    protected void agregarApoyo(ElementoApoyo apoyo) {
        elementos.add(apoyo);
    }

    /**
     * Busca un slot por su nombre.
     * Útil para que los elementos de apoyo accedan a los slots asignados.
     *
     * @param nombre Nombre del slot (ej: "VERBO", "CD")
     * @return El SlotPalabra o null si no existe
     */
    public SlotPalabra getSlotPorNombre(String nombre) {
        return slots.stream()
            .filter(s -> nombre.equals(s.getNombre()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Intenta asignar una palabra a algún slot vacío que coincida
     * @param palabra Palabra a intentar asignar
     * @param indice Índice de la palabra en la lista original
     * @return true si se asignó a algún slot, false si no coincide con ninguno
     */
    public boolean intentarAsignar(PalabraFlexion palabra, int indice) {
        for (SlotPalabra slot : slots) {
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
        return slots.stream().allMatch(SlotPalabra::estaAsignado);
    }

    /**
     * Obtiene los índices de las palabras usadas en esta estructura
     */
    public List<Integer> getIndicesUsados() {
        return slots.stream()
            .filter(SlotPalabra::estaAsignado)
            .map(SlotPalabra::getIndiceEnLista)
            .toList();
    }

    /**
     * Construye la lista de datos de visualización para el JSP.
     * El modo se decide ALEATORIAMENTE aquí (singleton con modo dinámico).
     * Itera sobre los elementos EN ORDEN para mantener la estructura de la frase.
     * El JSP recibirá textoFila1 y textoFila2 sin saber qué idioma es cada uno.
     */
    public List<DatoVisualizacion> construirDatosVisualizacion() {
        // Modo aleatorio cada vez que se construye
        ModoVisualizacion modo = ModoVisualizacion.aleatorio();
        log.debug("Construyendo datos con modo: {}", modo);

        List<DatoVisualizacion> datos = new ArrayList<>();

        for (Object elemento : elementos) {
            DatoVisualizacion dato = construirDato(elemento, modo);
            if (dato != null) {
                datos.add(dato);
            }
        }

        return datos;
    }

    /**
     * Construye un DatoVisualizacion para un elemento (slot o apoyo)
     */
    private DatoVisualizacion construirDato(Object elemento, ModoVisualizacion modo) {
        if (elemento instanceof SlotPalabra slot) {
            if (!slot.estaAsignado()) return null;

            PalabraFlexion palabra = slot.getPalabraAsignada();
            return DatoVisualizacion.builder()
                .textoFila1(slot.getTextoFila1(modo))
                .textoFila2(slot.getTextoFila2(modo))
                .id(palabra.getId())
                .tipo(FraseTipoPalabra.fromObject(palabra))
                .build();

        } else if (elemento instanceof ElementoApoyo apoyo) {
            PalabraFlexion objetoGenerado = apoyo.generarObjeto(this);

            return DatoVisualizacion.builder()
                .textoFila1(apoyo.getTextoFila1(modo, objetoGenerado))
                .textoFila2(apoyo.getTextoFila2(modo, objetoGenerado))
                .id(null)
                .tipo(null)
                .build();
        }

        return null;
    }

    /**
     * Limpia todos los slots para reutilización (singleton)
     */
    public void limpiar() {
        slots.forEach(SlotPalabra::limpiar);
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
