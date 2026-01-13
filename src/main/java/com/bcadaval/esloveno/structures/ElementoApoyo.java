package com.bcadaval.esloveno.structures;

import java.util.function.Function;

import com.bcadaval.esloveno.beans.base.PalabraFlexion;
import lombok.Builder;
import lombok.Data;

/**
 * Representa un elemento de apoyo en una frase (pronombre, número, etc.).
 * Similar a SlotPalabra pero:
 * - No tiene matcher (no busca en la lista de tarjetas)
 * - No tiene ID (no participa en SRS)
 * - El valor se genera dinámicamente basándose en los slots asignados
 *
 * Tiene los 4 extractores igual que SlotPalabra para controlar
 * qué se muestra en cada modo de visualización.
 */
@Data
@Builder
public class ElementoApoyo {

    /**
     * Nombre identificador del elemento (ej: "PRONOMBRE", "NUMERO")
     */
    private String nombre;

    /**
     * Función que genera el objeto de apoyo basándose en la estructura.
     * Recibe la EstructuraFrase para acceder a los slots asignados por nombre.
     * Ejemplo: frase -> pronombreService.getPronombre(
     *              (VerboFlexion) frase.getSlotPorNombre("VERBO").getPalabraAsignada())
     */
    private Function<EstructuraFrase, PalabraFlexion> generadorObjeto;

    /**
     * Extractor para modo ES_SL - Fila 1 (pregunta en español)
     * Recibe el objeto generado por generadorObjeto
     */
    private Function<PalabraFlexion, String> extractorDeEspanol;

    /**
     * Extractor para modo ES_SL - Fila 2 (respuesta en esloveno)
     */
    private Function<PalabraFlexion, String> extractorAEsloveno;

    /**
     * Extractor para modo SL_ES - Fila 1 (pregunta en esloveno)
     */
    private Function<PalabraFlexion, String> extractorDeEsloveno;

    /**
     * Extractor para modo SL_ES - Fila 2 (respuesta en español)
     */
    private Function<PalabraFlexion, String> extractorAEspanol;

    /**
     * Genera el objeto de apoyo usando el contexto de la frase
     */
    public PalabraFlexion generarObjeto(EstructuraFrase frase) {
        return generadorObjeto.apply(frase);
    }

    /**
     * Obtiene el texto para la fila 1 según el modo
     */
    public String getTextoFila1(ModoVisualizacion modo, PalabraFlexion objeto) {
        if (objeto == null) return "";
        return modo == ModoVisualizacion.ES_SL
            ? extractorDeEspanol.apply(objeto)
            : extractorDeEsloveno.apply(objeto);
    }

    /**
     * Obtiene el texto para la fila 2 según el modo
     */
    public String getTextoFila2(ModoVisualizacion modo, PalabraFlexion objeto) {
        if (objeto == null) return "";
        return modo == ModoVisualizacion.ES_SL
            ? extractorAEsloveno.apply(objeto)
            : extractorAEspanol.apply(objeto);
    }
}

