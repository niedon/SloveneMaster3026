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
     * Obtiene el valor como Integer
     */
    public Integer getValorAsInteger() {
        return valor != null ? Integer.parseInt(valor) : null;
    }

    /**
     * Obtiene el valor como Long
     */
    public Long getValorAsLong() {
        return valor != null ? Long.parseLong(valor) : null;
    }

    /**
     * Obtiene el valor como Double
     */
    public Double getValorAsDouble() {
        return valor != null ? Double.parseDouble(valor) : null;
    }

    /**
     * Obtiene el valor como Boolean
     */
    public Boolean getValorAsBoolean() {
        return valor != null ? Boolean.parseBoolean(valor) : null;
    }
}

