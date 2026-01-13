package com.bcadaval.esloveno.structures;

import java.util.function.Function;
import java.util.function.Predicate;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;

import lombok.Builder;
import lombok.Data;

/**
 * Representa un slot/posición dentro de una estructura de frase.
 * Cada slot define:
 * - Un matcher que determina si una PalabraFlexion cumple los requisitos
 * - 4 extractores para obtener el texto según el modo de visualización
 */
@Data
@Builder
public class SlotPalabra {

    /**
     * Nombre identificador del slot (ej: "VERBO", "CD", "SUJETO")
     */
    private String nombre;

    /**
     * Predicado que determina si una PalabraFlexion cumple los requisitos del slot
     */
    private Predicate<PalabraFlexion> matcher;

    /**
     * Extractor para modo ES_SL - Fila 1 (pregunta en español)
     * Ejemplo: getSignificado()
     */
    private Function<PalabraFlexion, String> extractorDeEspanol;

    /**
     * Extractor para modo ES_SL - Fila 2 (respuesta en esloveno)
     * Ejemplo: getAcentuado()
     */
    private Function<PalabraFlexion, String> extractorAEsloveno;

    /**
     * Extractor para modo SL_ES - Fila 1 (pregunta en esloveno)
     * Ejemplo: getFlexion()
     */
    private Function<PalabraFlexion, String> extractorDeEsloveno;

    /**
     * Extractor para modo SL_ES - Fila 2 (respuesta en español)
     * Ejemplo: getSignificado()
     */
    private Function<PalabraFlexion, String> extractorAEspanol;

    /**
     * Palabra asignada a este slot (null si no se ha asignado)
     */
    private PalabraFlexion palabraAsignada;

    /**
     * Índice de la palabra en la lista original de tarjetas
     */
    private Integer indiceEnLista;

    /**
     * Verifica si una palabra cumple los requisitos del slot
     * @param palabra Palabra a verificar
     * @return true si el slot está vacío y la palabra cumple el matcher
     */
    public boolean coincide(PalabraFlexion palabra) {
        return palabraAsignada == null && matcher.test(palabra);
    }

    /**
     * Asigna una palabra al slot
     * @param palabra Palabra a asignar
     * @param indice Índice en la lista original
     */
    public void asignar(PalabraFlexion palabra, int indice) {
        this.palabraAsignada = palabra;
        this.indiceEnLista = indice;
    }

    /**
     * Verifica si el slot tiene una palabra asignada
     */
    public boolean estaAsignado() {
        return palabraAsignada != null;
    }

    /**
     * Obtiene el texto para la fila 1 según el modo
     */
    public String getTextoFila1(ModoVisualizacion modo) {
        if (palabraAsignada == null) return "";
        return modo == ModoVisualizacion.ES_SL
            ? extractorDeEspanol.apply(palabraAsignada)
            : extractorDeEsloveno.apply(palabraAsignada);
    }

    /**
     * Obtiene el texto para la fila 2 según el modo
     */
    public String getTextoFila2(ModoVisualizacion modo) {
        if (palabraAsignada == null) return "";
        return modo == ModoVisualizacion.ES_SL
            ? extractorAEsloveno.apply(palabraAsignada)
            : extractorAEspanol.apply(palabraAsignada);
    }

    /**
     * Limpia el slot para reutilización
     */
    public void limpiar() {
        this.palabraAsignada = null;
        this.indiceEnLista = null;
    }
}

