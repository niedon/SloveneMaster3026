package com.bcadaval.esloveno.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para recibir todos los datos del formulario de configuración.
 * <p>
 * Agrupa las variables de configuración general y la lista de estructuras activas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionDTO {

    // --- Intervalos ---
    private String intervaloInicial;
    private String intervaloSegunda;
    private String intervaloReaprendizaje;

    // --- Factores ---
    private String factorFacilidadInicial;
    private String factorFacilidadMinimo;
    private String penalizacionFallo;

    // --- Límites Diarios ---
    private String maxTarjetasNuevas;
    private String maxTarjetasRevision;

    // --- Miscelánea ---
    private String mezclarTarjetas;

    // --- Estructuras ---
    /**
     * Lista de identificadores de las estructuras que deben estar activas.
     * Si una estructura no está en esta lista, se desactivará.
     */
    private List<String> estructuras;
}

