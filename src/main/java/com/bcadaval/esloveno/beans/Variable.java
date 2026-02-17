package com.bcadaval.esloveno.beans;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una variable de configuración del sistema.
 * Almacena parámetros configurables del algoritmo de repetición espaciada.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "variables")
public class Variable {

    /**
     * Clave única de la variable (ej: "MAX_TARJETAS_NUEVAS_DIA")
     */
    @Id
    private String clave;

    /**
     * Valor de la variable en formato String
     */
    private String valor;

    /**
     * Tipo de dato: INTEGER, LONG, DOUBLE, STRING
     */
    private String tipo;

    /**
     * Descripción legible de la variable
     */
    private String descripcion;

    /**
     * Obtiene el valor como Integer.
     * @return valor como Integer, o null si es null o no es parseable
     */
    public Integer getValorAsInteger() {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Obtiene el valor como Long.
     * @return valor como Long, o null si es null o no es parseable
     */
    public Long getValorAsLong() {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Long.parseLong(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Obtiene el valor como Double.
     * @return valor como Double, o null si es null o no es parseable
     */
    public Double getValorAsDouble() {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Double.parseDouble(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Obtiene el valor como Boolean.
     * @return true si valor es "true" (ignora mayúsculas), false en otro caso, null si valor es null
     */
    public Boolean getValorAsBoolean() {
        if (valor == null) return null;
        return Boolean.parseBoolean(valor.trim());
    }
}

