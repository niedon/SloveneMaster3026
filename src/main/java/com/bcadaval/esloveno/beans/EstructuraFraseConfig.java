package com.bcadaval.esloveno.beans;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuración de activación de estructuras de frase.
 * Cada registro representa una estructura de frase y su estado (activa/inactiva).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class EstructuraFraseConfig {

    /**
     * Identificador único de la estructura (ej: "VERBO_TRANSITIVO_ACUSATIVO")
     */
    @Id
    private String identificador;

    /**
     * Si la estructura está activa para el estudio
     */
    private Boolean activa;
}

