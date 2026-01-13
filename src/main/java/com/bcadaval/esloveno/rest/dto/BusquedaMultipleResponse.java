package com.bcadaval.esloveno.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para la respuesta de búsqueda múltiple de palabras
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusquedaMultipleResponse {

    /**
     * Indica si la búsqueda fue exitosa
     */
    private boolean exito;

    /**
     * Mensaje descriptivo del resultado
     */
    private String mensaje;

    /**
     * La palabra buscada
     */
    private String palabra;

    /**
     * Número total de resultados encontrados
     */
    private int totalResultados;

    /**
     * Lista de resultados encontrados
     */
    private List<ResultadoItem> resultados;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResultadoItem {
        /**
         * Lema de la palabra
         */
        private String lema;

        /**
         * Tipo en inglés (noun, verb, adjective, etc.)
         */
        private String tipo;

        /**
         * Tipo traducido al español
         */
        private String tipoEspanol;

        /**
         * Si el tipo está soportado para guardar
         */
        private boolean soportado;

        /**
         * Índice del resultado para identificarlo al guardar
         */
        private int indice;
    }
}

